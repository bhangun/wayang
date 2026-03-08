package tech.kayys.wayang.runtime.standalone.resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsCallableSupport {
    private ProjectsCallableSupport() {
    }

    static Map<String, Object> buildShareableDescriptor(
            Map<String, Object> project,
            String defaultTenant,
            String defaultOwnerUser,
            String visibilityPrivate,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        final Map<String, Object> metadata = mapValue(project.get("metadata"));
        final Map<String, Object> descriptor = new LinkedHashMap<>();
        descriptor.put("projectId", optionalStringValue(project.get("projectId")));
        descriptor.put("projectName", stringValue(project.get("projectName"), "Wayang Project"));
        descriptor.put("description", stringValue(project.get("description"), ""));
        descriptor.put("tenantId", stringValue(project.get("tenantId"), defaultTenant));
        descriptor.put("createdBy", stringValue(project.get("createdBy"), defaultOwnerUser));
        descriptor.put("updatedAt", optionalStringValue(project.get("updatedAt")));
        descriptor.put("savedAt", optionalStringValue(project.get("savedAt")));

        final Map<String, Object> access = mapValue(metadata.get("access"));
        descriptor.put("access", Map.of(
                "visibility", stringValue(access.get("visibility"), visibilityPrivate),
                "requireConsent", booleanValue(access.get("requireConsent"))));

        final Map<String, Object> callable = resolveCallableContract(
                metadata,
                resolveProjectVersion(project),
                reuseModeCallable,
                reuseModeAutonomous,
                entrypointManual,
                entrypointParameterized,
                entrypointEmpty,
                manualStartNodeTypes,
                parameterizedStartNodeTypes,
                autonomousTriggerNodeTypes);
        descriptor.put("callable", callable);
        return descriptor;
    }

    static Map<String, Object> ensureCallableSubWorkflowContract(
            String projectId,
            Map<String, Object> project,
            Map<String, Object> parentNodeConfiguration,
            String parentNodeId,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> callableEntrypointTypes,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        final Map<String, Object> metadata = mapValue(project.get("metadata"));
        final Map<String, Object> callable = resolveCallableContract(
                metadata,
                resolveProjectVersion(project),
                reuseModeCallable,
                reuseModeAutonomous,
                entrypointManual,
                entrypointParameterized,
                entrypointEmpty,
                manualStartNodeTypes,
                parameterizedStartNodeTypes,
                autonomousTriggerNodeTypes);
        final String mode = stringValue(callable.get("mode"), reuseModeAutonomous).toLowerCase();
        if (!reuseModeCallable.equals(mode)) {
            throw new IllegalArgumentException(
                    "Sub-workflow project '" + projectId + "' is not callable (mode=" + mode +
                            "). Use callable/manual/parameterized child workflows only.");
        }

        final Map<String, Object> entrypoint = mapValue(callable.get("entrypoint"));
        final String entrypointType = stringValue(entrypoint.get("type"), entrypointManual).toLowerCase();
        if (!callableEntrypointTypes.contains(entrypointType)) {
            throw new IllegalArgumentException(
                    "Sub-workflow project '" + projectId + "' has unsupported callable entrypoint type '" +
                            entrypointType + "'");
        }

        if (entrypointParameterized.equals(entrypointType)) {
            final Map<String, Object> inputs = mapValue(callable.get("inputs"));
            final List<Map<String, Object>> required = mapListValue(inputs.get("required"));
            final Map<String, Object> providedInputs = providedSubWorkflowInputs(parentNodeConfiguration);
            final List<String> missing = new ArrayList<>();
            final List<String> invalidType = new ArrayList<>();
            for (Map<String, Object> requiredInput : required) {
                final String name = optionalStringValue(requiredInput.get("name"));
                if (name == null) {
                    continue;
                }
                if (!providedInputs.containsKey(name)) {
                    missing.add(name);
                    continue;
                }
                final String expectedType = optionalStringValue(requiredInput.get("type"));
                if (expectedType != null && !isValueTypeCompatible(providedInputs.get(name), expectedType)) {
                    invalidType.add(name + ":" + expectedType);
                }
            }
            if (!missing.isEmpty()) {
                throw new IllegalArgumentException(
                        "Sub-workflow node '" + parentNodeId + "' missing required parameterized inputs for child '" +
                                projectId + "': " + String.join(", ", missing));
            }
            if (!invalidType.isEmpty()) {
                throw new IllegalArgumentException(
                        "Sub-workflow node '" + parentNodeId + "' has invalid input types for child '" +
                                projectId + "': " + String.join(", ", invalidType));
            }
        }
        validatePinnedProjectVersion(projectId, callable, parentNodeConfiguration, parentNodeId);
        validateOutputBindings(projectId, callable, parentNodeConfiguration, parentNodeId);
        return callable;
    }

    static Map<String, Object> resolveCallableContract(
            Map<String, Object> metadata,
            String fallbackVersion,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        final Map<String, Object> reuse = mapValue(metadata.get("reuse"));
        final Map<String, Object> rawEntrypoint = mapValue(reuse.get("entrypoint"));
        final Map<String, Object> rawContract = mapValue(reuse.get("contract"));
        final Map<String, Object> rawInputs = mapValue(firstNonNull(rawContract.get("inputs"), reuse.get("inputs")));
        final Map<String, Object> rawOutput = mapValue(firstNonNull(rawContract.get("output"), reuse.get("output")));

        final Map<String, Object> childSpec = extractProjectWayangSpec(metadata);
        final Map<String, Object> workflow = mapValue(childSpec.get("workflow"));
        final List<Map<String, Object>> nodes = mapListValue(workflow.get("nodes"));
        final List<Map<String, Object>> connections = mapListValue(workflow.get("connections"));
        final List<String> entryNodeIds = ProjectsSubWorkflowSupport.inferEntryNodeIds(workflow, nodes, connections);
        final List<String> entryNodeTypes = entryNodeIds.stream()
                .map(id -> findNodeTypeById(nodes, id))
                .filter(type -> type != null && !type.isBlank())
                .toList();
        final String inferredEntrypointType = inferEntrypointType(
                entryNodeTypes,
                nodes,
                entrypointManual,
                entrypointParameterized,
                entrypointEmpty,
                manualStartNodeTypes,
                parameterizedStartNodeTypes,
                autonomousTriggerNodeTypes);
        final String inferredMode = inferReuseMode(
                inferredEntrypointType,
                entryNodeTypes,
                reuseModeCallable,
                reuseModeAutonomous,
                entrypointManual,
                entrypointParameterized,
                entrypointEmpty,
                autonomousTriggerNodeTypes);
        final String version = firstNonBlank(
                optionalStringValue(reuse.get("version")),
                optionalStringValue(metadata.get("version")),
                fallbackVersion);

        final String mode = stringValue(reuse.get("mode"), inferredMode).toLowerCase();
        final String entrypointType = stringValue(rawEntrypoint.get("type"), inferredEntrypointType).toLowerCase();

        final Map<String, Object> entrypoint = new LinkedHashMap<>();
        entrypoint.put("type", entrypointType);
        entrypoint.put("nodeIds", entryNodeIds);
        entrypoint.put("nodeTypes", entryNodeTypes);

        final List<Map<String, Object>> requiredInputs = normalizeTypedFields(rawInputs.get("required"));
        final List<Map<String, Object>> optionalInputs = normalizeTypedFields(rawInputs.get("optional"));
        final Map<String, Object> output = normalizeOutputContract(rawOutput);

        final Map<String, Object> callable = new LinkedHashMap<>();
        callable.put("enabled", booleanValue(firstNonNull(reuse.get("enabled"), reuseModeCallable.equals(mode))));
        callable.put("mode", mode);
        callable.put("entrypoint", entrypoint);
        callable.put("inputs", Map.of("required", requiredInputs, "optional", optionalInputs));
        callable.put("output", output);
        if (version != null) {
            callable.put("version", version);
        }
        return callable;
    }

    static void ensureSubWorkflowAccess(
            String projectId,
            Map<String, Object> project,
            String requesterTenantId,
            String requesterUserId,
            String defaultTenant,
            String defaultOwnerUser,
            String visibilityPrivate,
            String visibilityTenant,
            String visibilityExplicit,
            String visibilityPublic,
            String requiredPermissionExecuteSubworkflow) {
        final String effectiveRequesterTenant = stringValue(requesterTenantId, defaultTenant);
        final String requesterUser = optionalStringValue(requesterUserId);
        final String projectTenant = stringValue(project.get("tenantId"), defaultTenant);
        final String projectOwner = stringValue(project.get("createdBy"), defaultOwnerUser);
        final Map<String, Object> metadata = mapValue(project.get("metadata"));
        final Map<String, Object> access = mapValue(metadata.get("access"));

        final String ownerTenant = stringValue(access.get("ownerTenantId"), projectTenant);
        final String ownerUser = stringValue(access.get("ownerUserId"), projectOwner);
        final String visibility = stringValue(access.get("visibility"), visibilityPrivate).toLowerCase();
        final boolean requireConsent = booleanValue(access.get("requireConsent"));
        final List<String> sharedUsers = stringListValue(access.get("sharedWithUsers"));
        final List<String> sharedTenants = stringListValue(access.get("sharedWithTenants"));

        final boolean isOwner = ownerTenant.equals(effectiveRequesterTenant)
                && requesterUser != null
                && requesterUser.equals(ownerUser);
        final boolean userShared = requesterUser != null && sharedUsers.contains(requesterUser);
        final boolean tenantShared = sharedTenants.contains(effectiveRequesterTenant);
        final boolean sameTenant = ownerTenant.equals(effectiveRequesterTenant);

        boolean allowed = isOwner || userShared || tenantShared;
        switch (visibility) {
            case "public" -> allowed = true;
            case "tenant" -> allowed = allowed || sameTenant;
            case "explicit" -> {
            }
            case "private" -> {
                if (requesterUser == null) {
                    allowed = allowed || sameTenant;
                }
            }
            default -> {
            }
        }

        if (requireConsent && !isOwner) {
            allowed = allowed && hasConsentGrant(access, effectiveRequesterTenant, requesterUser, requiredPermissionExecuteSubworkflow);
        }

        if (!allowed) {
            throw new IllegalArgumentException(
                    "Access denied for sub-workflow project '" + projectId + "'. " +
                            "Owner/share consent does not allow '" + effectiveRequesterTenant + ":" +
                            (requesterUser != null ? requesterUser : "anonymous") + "'");
        }
    }

    private static boolean hasConsentGrant(
            Map<String, Object> access,
            String requesterTenantId,
            String requesterUserId,
            String requiredPermissionExecuteSubworkflow) {
        final List<Map<String, Object>> grants = mapListValue(access.get("consentGrants"));
        for (Map<String, Object> grant : grants) {
            final String grantTenant = optionalStringValue(grant.get("tenantId"));
            final String grantUser = optionalStringValue(grant.get("userId"));
            final String permission = stringValue(grant.get("permission"), requiredPermissionExecuteSubworkflow);
            if (!requiredPermissionExecuteSubworkflow.equalsIgnoreCase(permission)) {
                continue;
            }
            final boolean tenantMatch = grantTenant == null || grantTenant.equals(requesterTenantId);
            final boolean userMatch = grantUser == null
                    || (requesterUserId != null && grantUser.equals(requesterUserId));
            if (tenantMatch && userMatch) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Object> providedSubWorkflowInputs(Map<String, Object> parentNodeConfiguration) {
        final Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.putAll(mapValue(parentNodeConfiguration.get("inputs")));
        inputs.putAll(mapValue(parentNodeConfiguration.get("parameters")));
        inputs.putAll(mapValue(parentNodeConfiguration.get("inputBindings")));
        return inputs;
    }

    private static void validatePinnedProjectVersion(
            String projectId,
            Map<String, Object> callable,
            Map<String, Object> parentNodeConfiguration,
            String parentNodeId) {
        final String requestedVersion = firstNonBlank(
                optionalStringValue(parentNodeConfiguration.get("projectVersion")),
                optionalStringValue(parentNodeConfiguration.get("childVersion")),
                optionalStringValue(parentNodeConfiguration.get("version")));
        if (requestedVersion == null) {
            return;
        }
        final String actualVersion = optionalStringValue(callable.get("version"));
        if (actualVersion == null) {
            throw new IllegalArgumentException(
                    "Sub-workflow node '" + parentNodeId + "' pins child '" + projectId +
                            "' version '" + requestedVersion + "', but child has no published version");
        }
        if (!requestedVersion.equals(actualVersion)) {
            throw new IllegalArgumentException(
                    "Sub-workflow node '" + parentNodeId + "' version mismatch for child '" + projectId +
                            "': requested '" + requestedVersion + "', available '" + actualVersion + "'");
        }
    }

    private static void validateOutputBindings(
            String projectId,
            Map<String, Object> callable,
            Map<String, Object> parentNodeConfiguration,
            String parentNodeId) {
        final Map<String, Object> bindings = mapValue(parentNodeConfiguration.get("outputBindings"));
        if (bindings.isEmpty()) {
            return;
        }
        final Map<String, Object> outputContract = mapValue(callable.get("output"));
        final Map<String, Object> outputProperties = mapValue(outputContract.get("properties"));
        if (outputProperties.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sub-workflow node '" + parentNodeId + "' defines outputBindings for child '" + projectId +
                            "', but child callable output contract is not declared");
        }
        final List<String> invalid = new ArrayList<>();
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            final String sourceField = optionalStringValue(entry.getKey());
            final String targetField = optionalStringValue(entry.getValue());
            if (sourceField == null || targetField == null) {
                invalid.add(String.valueOf(entry.getKey()));
                continue;
            }
            if (!"*".equals(sourceField) && !outputProperties.containsKey(sourceField)) {
                invalid.add(sourceField);
            }
        }
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sub-workflow node '" + parentNodeId + "' has invalid outputBindings for child '" +
                            projectId + "': " + String.join(", ", invalid));
        }
    }

    private static boolean isValueTypeCompatible(Object value, String expectedTypeRaw) {
        final String expectedType = expectedTypeRaw.toLowerCase();
        if (value == null) {
            return true;
        }
        return switch (expectedType) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "integer", "int", "long" ->
                    value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long;
            case "boolean", "bool" -> value instanceof Boolean;
            case "array", "list" -> value instanceof List<?>;
            case "object", "map" -> value instanceof Map<?, ?>;
            default -> true;
        };
    }

    private static String resolveProjectVersion(Map<String, Object> project) {
        final Map<String, Object> metadata = mapValue(project.get("metadata"));
        final Map<String, Object> reuse = mapValue(metadata.get("reuse"));
        return firstNonBlank(
                optionalStringValue(reuse.get("version")),
                optionalStringValue(metadata.get("version")),
                optionalStringValue(project.get("savedAt")),
                optionalStringValue(project.get("updatedAt")));
    }

    private static List<Map<String, Object>> normalizeTypedFields(Object raw) {
        final List<Map<String, Object>> items = mapListValue(raw);
        final List<Map<String, Object>> normalized = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            final String name = optionalStringValue(item.get("name"));
            if (name == null) {
                continue;
            }
            final Map<String, Object> field = new LinkedHashMap<>();
            field.put("name", name);
            field.put("type", stringValue(item.get("type"), "string"));
            final String description = optionalStringValue(item.get("description"));
            if (description != null) {
                field.put("description", description);
            }
            final Map<String, Object> constraints = mapValue(item.get("constraints"));
            if (!constraints.isEmpty()) {
                field.put("constraints", constraints);
            }
            normalized.add(field);
        }
        return normalized;
    }

    private static Map<String, Object> normalizeOutputContract(Map<String, Object> rawOutput) {
        if (rawOutput.isEmpty()) {
            return Map.of();
        }
        final Map<String, Object> output = new LinkedHashMap<>();
        output.put("type", stringValue(rawOutput.get("type"), "object"));
        final String description = optionalStringValue(rawOutput.get("description"));
        if (description != null) {
            output.put("description", description);
        }
        final Map<String, Object> properties = mapValue(rawOutput.get("properties"));
        if (!properties.isEmpty()) {
            output.put("properties", properties);
        }
        final List<String> required = stringListValue(rawOutput.get("required"));
        if (!required.isEmpty()) {
            output.put("required", required);
        }
        return output;
    }

    private static String inferReuseMode(
            String entrypointType,
            List<String> entryNodeTypes,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> autonomousTriggerNodeTypes) {
        if (entrypointEmpty.equals(entrypointType)
                || entrypointManual.equals(entrypointType)
                || entrypointParameterized.equals(entrypointType)) {
            return reuseModeCallable;
        }
        for (String type : entryNodeTypes) {
            if (autonomousTriggerNodeTypes.contains(type.toLowerCase())) {
                return reuseModeAutonomous;
            }
        }
        return reuseModeAutonomous;
    }

    private static String inferEntrypointType(
            List<String> entryNodeTypes,
            List<Map<String, Object>> nodes,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        if (nodes.isEmpty()) {
            return entrypointEmpty;
        }
        if (entryNodeTypes.isEmpty()) {
            return entrypointManual;
        }
        boolean hasParameterized = false;
        for (String typeRaw : entryNodeTypes) {
            final String type = typeRaw.toLowerCase();
            if (parameterizedStartNodeTypes.contains(type)) {
                hasParameterized = true;
                continue;
            }
            if (manualStartNodeTypes.contains(type)) {
                continue;
            }
            if (autonomousTriggerNodeTypes.contains(type)) {
                return "trigger";
            }
        }
        return hasParameterized ? entrypointParameterized : entrypointManual;
    }

    private static String findNodeTypeById(List<Map<String, Object>> nodes, String nodeId) {
        for (Map<String, Object> node : nodes) {
            if (nodeId.equals(resolveNodeId(node))) {
                return stringValue(node.get("type"), "");
            }
        }
        return null;
    }

    private static Map<String, Object> extractProjectWayangSpec(Map<String, Object> metadata) {
        final Object rawWayangSpec = firstNonNull(
                metadata.get("wayangSpec"),
                metadata.get("spec"),
                mapValue(metadata.get("wayangProject")).get("wayangSpec"),
                mapValue(metadata.get("wayangProject")).get("spec"));
        return rawWayangSpec != null ? ProjectsSubWorkflowSupport.normalizeAsWayangSpec(rawWayangSpec) : Map.of();
    }

    private static String resolveNodeId(Map<String, Object> node) {
        final String id = optionalStringValue(node.get("id"));
        if (id != null) {
            return id;
        }
        return optionalStringValue(mapValue(node.get("metadata")).get("id"));
    }

    private static Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            final Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
            return result;
        }
        return new LinkedHashMap<>();
    }

    private static List<Map<String, Object>> mapListValue(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return new ArrayList<>();
        }
        final List<Map<String, Object>> values = new ArrayList<>(list.size());
        for (Object item : list) {
            values.add(mapValue(item));
        }
        return values;
    }

    private static List<String> stringListValue(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        final List<String> values = new ArrayList<>();
        for (Object item : list) {
            final String value = optionalStringValue(item);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = String.valueOf(raw).trim();
        return value.isEmpty() ? null : value;
    }

    private static String stringValue(Object raw, String fallback) {
        final String value = optionalStringValue(raw);
        return value != null ? value : fallback;
    }

    private static boolean booleanValue(Object raw) {
        if (raw instanceof Boolean value) {
            return value;
        }
        if (raw instanceof Number numeric) {
            return numeric.intValue() != 0;
        }
        return raw != null && Boolean.parseBoolean(String.valueOf(raw));
    }
}
