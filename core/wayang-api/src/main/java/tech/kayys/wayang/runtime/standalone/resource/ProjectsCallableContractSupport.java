package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsCallableContractSupport {
    private ProjectsCallableContractSupport() {
    }

    static Response listShareableProjects(
            String tenantId,
            String userId,
            String mode,
            String excludeProjectId,
            String defaultTenant,
            String defaultOwnerUser,
            String visibilityPrivate,
            String visibilityTenant,
            String visibilityExplicit,
            String visibilityPublic,
            String requiredPermissionExecuteSubworkflow,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        try {
            final String requestedMode = stringValue(mode, reuseModeCallable).toLowerCase();
            final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
            final List<Map<String, Object>> shareables = new ArrayList<>();
            for (Map<String, Object> project : projects) {
                final String projectId = optionalStringValue(project.get("projectId"));
                if (projectId == null) {
                    continue;
                }
                if (excludeProjectId != null && excludeProjectId.equals(projectId)) {
                    continue;
                }
                try {
                    ProjectsCallableSupport.ensureSubWorkflowAccess(
                            projectId,
                            project,
                            tenantId,
                            userId,
                            defaultTenant,
                            defaultOwnerUser,
                            visibilityPrivate,
                            visibilityTenant,
                            visibilityExplicit,
                            visibilityPublic,
                            requiredPermissionExecuteSubworkflow);
                } catch (IllegalArgumentException denied) {
                    continue;
                }
                final Map<String, Object> descriptor = ProjectsCallableSupport.buildShareableDescriptor(
                        project,
                        defaultTenant,
                        defaultOwnerUser,
                        visibilityPrivate,
                        reuseModeCallable,
                        reuseModeAutonomous,
                        entrypointManual,
                        entrypointParameterized,
                        entrypointEmpty,
                        manualStartNodeTypes,
                        parameterizedStartNodeTypes,
                        autonomousTriggerNodeTypes);
                final Map<String, Object> callable = mapValue(descriptor.get("callable"));
                final String effectiveMode = stringValue(callable.get("mode"), reuseModeAutonomous).toLowerCase();
                if (!requestedMode.equals(effectiveMode)) {
                    continue;
                }
                shareables.add(descriptor);
            }
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("mode", requestedMode);
            payload.put("count", shareables.size());
            payload.put("projects", shareables);
            return Response.ok(payload).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to list shareable projects", "message", e.getMessage()))
                    .build();
        }
    }

    static Response getCallableContract(
            String projectId,
            String tenantId,
            String userId,
            String defaultTenant,
            String defaultOwnerUser,
            String visibilityPrivate,
            String visibilityTenant,
            String visibilityExplicit,
            String visibilityPublic,
            String requiredPermissionExecuteSubworkflow,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        try {
            final Map<String, Object> project = ProjectsExecutionSupport.findProjectById(projectId);
            if (project == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Project not found: " + projectId))
                        .build();
            }
            ProjectsCallableSupport.ensureSubWorkflowAccess(
                    projectId,
                    project,
                    tenantId,
                    userId,
                    defaultTenant,
                    defaultOwnerUser,
                    visibilityPrivate,
                    visibilityTenant,
                    visibilityExplicit,
                    visibilityPublic,
                    requiredPermissionExecuteSubworkflow);
            final Map<String, Object> descriptor = ProjectsCallableSupport.buildShareableDescriptor(
                    project,
                    defaultTenant,
                    defaultOwnerUser,
                    visibilityPrivate,
                    reuseModeCallable,
                    reuseModeAutonomous,
                    entrypointManual,
                    entrypointParameterized,
                    entrypointEmpty,
                    manualStartNodeTypes,
                    parameterizedStartNodeTypes,
                    autonomousTriggerNodeTypes);
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("projectId", projectId);
            payload.put("callable", descriptor.get("callable"));
            return Response.ok(payload).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read callable contract", "message", e.getMessage()))
                    .build();
        }
    }

    static Response validateCallableContract(
            String projectId,
            String tenantId,
            String userId,
            Map<String, Object> request,
            String defaultTenant,
            String defaultOwnerUser,
            String visibilityPrivate,
            String visibilityTenant,
            String visibilityExplicit,
            String visibilityPublic,
            String requiredPermissionExecuteSubworkflow,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> callableEntrypointTypes,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        try {
            final Map<String, Object> project = ProjectsExecutionSupport.findProjectById(projectId);
            if (project == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Project not found: " + projectId))
                        .build();
            }
            ProjectsCallableSupport.ensureSubWorkflowAccess(
                    projectId,
                    project,
                    tenantId,
                    userId,
                    defaultTenant,
                    defaultOwnerUser,
                    visibilityPrivate,
                    visibilityTenant,
                    visibilityExplicit,
                    visibilityPublic,
                    requiredPermissionExecuteSubworkflow);
            final Map<String, Object> body = request != null ? request : Map.of();
            final Map<String, Object> nodeConfiguration = cloneMap(body.get("configuration"));
            if (nodeConfiguration.isEmpty()) {
                nodeConfiguration.put("projectId", projectId);
            }
            final String nodeId = stringValue(body.get("nodeId"), "validate-callable");
            final Map<String, Object> callable = ProjectsCallableSupport.ensureCallableSubWorkflowContract(
                    projectId,
                    project,
                    nodeConfiguration,
                    nodeId,
                    reuseModeCallable,
                    reuseModeAutonomous,
                    entrypointManual,
                    entrypointParameterized,
                    entrypointEmpty,
                    callableEntrypointTypes,
                    manualStartNodeTypes,
                    parameterizedStartNodeTypes,
                    autonomousTriggerNodeTypes);
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("projectId", projectId);
            payload.put("valid", true);
            payload.put("callable", callable);
            payload.put("checkedAt", Instant.now().toString());
            return Response.ok(payload).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("valid", false, "message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to validate callable contract", "message", e.getMessage()))
                    .build();
        }
    }

    static Response previewOutputBindings(
            String projectId,
            String tenantId,
            String userId,
            Map<String, Object> request,
            String defaultTenant,
            String defaultOwnerUser,
            String visibilityPrivate,
            String visibilityTenant,
            String visibilityExplicit,
            String visibilityPublic,
            String requiredPermissionExecuteSubworkflow,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        try {
            final Map<String, Object> project = ProjectsExecutionSupport.findProjectById(projectId);
            if (project == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Project not found: " + projectId))
                        .build();
            }
            ProjectsCallableSupport.ensureSubWorkflowAccess(
                    projectId,
                    project,
                    tenantId,
                    userId,
                    defaultTenant,
                    defaultOwnerUser,
                    visibilityPrivate,
                    visibilityTenant,
                    visibilityExplicit,
                    visibilityPublic,
                    requiredPermissionExecuteSubworkflow);
            final Map<String, Object> body = request != null ? request : Map.of();
            final Map<String, Object> configuration = cloneMap(body.get("configuration"));
            if (configuration.isEmpty()) {
                configuration.put("projectId", projectId);
            }
            final Map<String, Object> metadata = mapValue(project.get("metadata"));
            final Map<String, Object> callable = ProjectsCallableSupport.resolveCallableContract(
                    metadata,
                    optionalStringValue(metadata.get("version")),
                    reuseModeCallable,
                    reuseModeAutonomous,
                    entrypointManual,
                    entrypointParameterized,
                    entrypointEmpty,
                    manualStartNodeTypes,
                    parameterizedStartNodeTypes,
                    autonomousTriggerNodeTypes);
            final Map<String, Object> output = mapValue(callable.get("output"));
            final Map<String, Object> outputProperties = mapValue(output.get("properties"));
            final Map<String, Object> requestedBindings = mapValue(configuration.get("outputBindings"));

            final List<String> validSources = new ArrayList<>(outputProperties.keySet());
            validSources.add("*");
            final List<String> invalidBindings = new ArrayList<>();
            final Map<String, Object> normalizedBindings = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : requestedBindings.entrySet()) {
                final String source = optionalStringValue(entry.getKey());
                final String target = optionalStringValue(entry.getValue());
                if (source == null || target == null) {
                    invalidBindings.add(String.valueOf(entry.getKey()));
                    continue;
                }
                if (!"*".equals(source) && !outputProperties.containsKey(source)) {
                    invalidBindings.add(source);
                    continue;
                }
                normalizedBindings.put(source, target);
            }

            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("projectId", projectId);
            payload.put("callableOutput", output);
            payload.put("validSources", validSources.stream().sorted().toList());
            payload.put("bindings", normalizedBindings);
            payload.put("invalidSources", invalidBindings.stream().sorted().toList());
            payload.put("valid", invalidBindings.isEmpty());
            return Response.ok(payload).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("valid", false, "message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to preview output bindings", "message", e.getMessage()))
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            final Map<String, Object> result = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return new LinkedHashMap<>();
    }

    private static Map<String, Object> cloneMap(Object raw) {
        return new LinkedHashMap<>(mapValue(raw));
    }

    private static String stringValue(Object raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }
}
