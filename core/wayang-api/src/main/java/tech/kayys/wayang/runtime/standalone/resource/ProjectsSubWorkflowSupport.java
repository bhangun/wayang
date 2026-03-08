package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

final class ProjectsSubWorkflowSupport {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ProjectsSubWorkflowSupport() {
    }

    interface ChildSpecResolver {
        Map<String, Object> resolve(
                String projectId,
                String tenantId,
                String requesterUserId,
                Map<String, Object> parentNodeConfiguration,
                String parentNodeId) throws IOException;
    }

    static Map<String, Object> expandReferences(
            Map<String, Object> rawSpecPayload,
            String projectId,
            int maxDepth,
            String tenantId,
            String requesterUserId,
            Set<String> subWorkflowNodeTypes,
            ChildSpecResolver childSpecResolver) throws IOException {
        if (rawSpecPayload == null || rawSpecPayload.isEmpty()) {
            return Map.of();
        }
        final Map<String, Object> spec = OBJECT_MAPPER.convertValue(
                rawSpecPayload,
                new TypeReference<Map<String, Object>>() {});
        final Set<String> ancestry = new HashSet<>();
        if (projectId != null && !projectId.isBlank()) {
            ancestry.add(projectId);
        }
        return expandReferencesRecursive(
                spec,
                projectId,
                0,
                maxDepth,
                ancestry,
                tenantId,
                requesterUserId,
                subWorkflowNodeTypes,
                childSpecResolver);
    }

    static Map<String, Object> summarizeBindingSummary(Map<String, Object> configuration) {
        final Map<String, Object> inputBindings = mapValue(configuration.get("inputBindings"));
        final Map<String, Object> inputs = mapValue(configuration.get("inputs"));
        final Map<String, Object> parameters = mapValue(configuration.get("parameters"));
        final Map<String, Object> outputBindings = mapValue(configuration.get("outputBindings"));
        final Set<String> inputKeys = new TreeSet<>();
        inputKeys.addAll(inputBindings.keySet());
        inputKeys.addAll(inputs.keySet());
        inputKeys.addAll(parameters.keySet());
        final List<String> outputSources = outputBindings.keySet().stream().sorted().toList();
        final List<String> outputTargets = outputBindings.values().stream()
                .map(ProjectsSubWorkflowSupport::optionalStringValue)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("inputKeys", new ArrayList<>(inputKeys));
        summary.put("inputCount", inputKeys.size());
        summary.put("outputBindingSources", outputSources);
        summary.put("outputBindingTargets", outputTargets);
        summary.put("outputBindingCount", outputSources.size());
        final String invokeMode = optionalStringValue(configuration.get("invokeMode"));
        if (invokeMode != null) {
            summary.put("invokeMode", invokeMode);
        }
        if (configuration.containsKey("waitForCompletion")) {
            summary.put("waitForCompletion", booleanValue(configuration.get("waitForCompletion")));
        }
        return summary;
    }

    static Map<String, Object> summarizeResolution(Map<String, Object> specPayload) {
        final Map<String, Object> workflow = mapValue(specPayload.get("workflow"));
        if (workflow.isEmpty()) {
            return Map.of();
        }
        final List<Map<String, Object>> nodes = mapListValue(workflow.get("nodes"));
        final long expandedNodes = nodes.stream()
                .map(node -> mapValue(node.get("configuration")))
                .filter(cfg -> cfg.containsKey("_subWorkflow"))
                .count();
        final List<Map<String, Object>> children = mapListValue(workflow.get("children"));
        final long childrenResolved = children.stream()
                .map(item -> optionalStringValue(item.get("projectId")))
                .filter(value -> value != null && !value.isBlank())
                .count();
        if (expandedNodes == 0 && children.isEmpty()) {
            return Map.of();
        }
        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("childReferences", children.size());
        summary.put("childrenResolved", childrenResolved);
        summary.put("expandedNodeCount", expandedNodes);
        summary.put("maxDepthApplied", longValue(mapValue(workflow.get("metadata")).get("maxSubWorkflowDepth"), 0L));
        final List<Map<String, Object>> trace = new ArrayList<>();
        for (Map<String, Object> child : children) {
            final Map<String, Object> item = new LinkedHashMap<>();
            item.put("childId", optionalStringValue(child.get("childId")));
            item.put("projectId", optionalStringValue(child.get("projectId")));
            final Map<String, Object> metadata = mapValue(child.get("metadata"));
            item.put("depth", longValue(metadata.get("depth"), 0L));
            item.put("entrypointType", optionalStringValue(metadata.get("entrypointType")));
            item.put("callableMode", optionalStringValue(metadata.get("callableMode")));
            item.put("version", optionalStringValue(metadata.get("version")));
            item.put("parentNodeId", optionalStringValue(metadata.get("parentNodeId")));
            item.put("parentProjectId", optionalStringValue(metadata.get("parentProjectId")));
            item.put("bindingSummary", mapValue(metadata.get("bindingSummary")));
            trace.add(item);
        }
        if (!trace.isEmpty()) {
            summary.put("trace", trace);
        }
        return summary;
    }

    static Map<String, Object> normalizeAsWayangSpec(Object rawSpec) {
        final Map<String, Object> spec = mapValue(rawSpec);
        if (spec.containsKey("workflow")) {
            return spec;
        }
        if (spec.containsKey("nodes") || spec.containsKey("connections")) {
            final Map<String, Object> workflow = new LinkedHashMap<>();
            workflow.put("nodes", mapListValue(spec.get("nodes")));
            workflow.put("connections", mapListValue(spec.get("connections")));
            final Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("specVersion", "1.0.0");
            wrapper.put("workflow", workflow);
            return wrapper;
        }
        return spec;
    }

    static List<String> inferEntryNodeIds(
            Map<String, Object> workflow,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> connections) {
        return resolveEntryNodeIds(workflow, nodes, connections);
    }

    private static Map<String, Object> expandReferencesRecursive(
            Map<String, Object> specPayload,
            String currentProjectId,
            int depth,
            int maxDepth,
            Set<String> ancestry,
            String tenantId,
            String requesterUserId,
            Set<String> subWorkflowNodeTypes,
            ChildSpecResolver childSpecResolver) throws IOException {
        final Map<String, Object> workflow = mapValue(specPayload.get("workflow"));
        if (workflow.isEmpty()) {
            return specPayload;
        }

        final List<Map<String, Object>> nodes = mapListValue(workflow.get("nodes"));
        final List<Map<String, Object>> connections = mapListValue(workflow.get("connections"));
        if (nodes.isEmpty()) {
            return specPayload;
        }

        final Map<String, ReplacementGraph> replacementByNodeId = new LinkedHashMap<>();
        final List<Map<String, Object>> expandedNodes = new ArrayList<>();
        final List<Map<String, Object>> expandedConnections = new ArrayList<>();
        final List<Map<String, Object>> children = mapListValue(workflow.get("children"));
        boolean changed = false;

        for (int i = 0; i < nodes.size(); i++) {
            final Map<String, Object> node = cloneMap(nodes.get(i));
            final String nodeType = optionalStringValue(node.get("type"));
            final String nodeId = ProjectsNodeGraphSupport.resolveNodeId(node, i);
            if (nodeType == null || !isSubWorkflowNodeType(nodeType, subWorkflowNodeTypes)) {
                expandedNodes.add(node);
                continue;
            }
            changed = true;

            if (depth + 1 > maxDepth) {
                throw new IllegalArgumentException(
                        "Sub-workflow depth limit exceeded at node '" + nodeId + "'. maxSubWorkflowDepth=" + maxDepth);
            }

            final Map<String, Object> configuration = ProjectsNodeGraphSupport.mergedNodeConfiguration(node);
            final String childProjectId = firstNonBlank(
                    optionalStringValue(configuration.get("projectId")),
                    optionalStringValue(configuration.get("childProjectId")),
                    optionalStringValue(node.get("projectId")));
            final Object inlineSpecRaw = firstNonNull(
                    configuration.get("wayangSpec"),
                    configuration.get("spec"),
                    configuration.get("subWorkflowSpec"),
                    configuration.get("workflowSpec"),
                    node.get("subWorkflow"));

            final Map<String, Object> childSpec = inlineSpecRaw != null
                    ? normalizeAsWayangSpec(inlineSpecRaw)
                    : childSpecResolver.resolve(childProjectId, tenantId, requesterUserId, configuration, nodeId);

            if (childSpec.isEmpty()) {
                throw new IllegalArgumentException(
                        "Sub-workflow node '" + nodeId + "' does not contain a resolvable child workflow spec");
            }

            final String resolvedChildProjectId = firstNonBlank(
                    childProjectId,
                    optionalStringValue(childSpec.get("projectId")));
            if (resolvedChildProjectId != null && ancestry.contains(resolvedChildProjectId)) {
                throw new IllegalArgumentException(
                        "Sub-workflow cycle detected for projectId '" + resolvedChildProjectId + "' at node '" + nodeId + "'");
            }
            final String nextProjectId = firstNonBlank(resolvedChildProjectId, currentProjectId);

            final Set<String> nextAncestry = new HashSet<>(ancestry);
            if (resolvedChildProjectId != null) {
                nextAncestry.add(resolvedChildProjectId);
            }
            final Map<String, Object> expandedChildSpec = expandReferencesRecursive(
                    childSpec,
                    nextProjectId,
                    depth + 1,
                    maxDepth,
                    nextAncestry,
                    tenantId,
                    requesterUserId,
                    subWorkflowNodeTypes,
                    childSpecResolver);

            final Map<String, Object> childWorkflow = mapValue(expandedChildSpec.get("workflow"));
            final List<Map<String, Object>> childNodes = mapListValue(childWorkflow.get("nodes"));
            if (childNodes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Sub-workflow node '" + nodeId + "' resolved to an empty workflow");
            }
            final List<Map<String, Object>> childConnections = mapListValue(childWorkflow.get("connections"));

            final String prefix = nodeId + "__";
            final List<Map<String, Object>> prefixedChildNodes = new ArrayList<>();
            for (int c = 0; c < childNodes.size(); c++) {
                final Map<String, Object> childNode = cloneMap(childNodes.get(c));
                final String childNodeId = ProjectsNodeGraphSupport.resolveNodeId(childNode, c);
                final String prefixedNodeId = prefix + childNodeId;
                ProjectsNodeGraphSupport.setNodeId(childNode, prefixedNodeId);
                final Map<String, Object> childConfig = ProjectsNodeGraphSupport.mergedNodeConfiguration(childNode);
                childConfig.put("_subWorkflow", Map.of(
                        "parentNodeId", nodeId,
                        "parentProjectId", currentProjectId != null ? currentProjectId : "",
                        "childProjectId", resolvedChildProjectId != null ? resolvedChildProjectId : "",
                        "depth", depth + 1));
                childNode.put("configuration", childConfig);
                prefixedChildNodes.add(childNode);
            }

            final List<Map<String, Object>> prefixedChildConnections = new ArrayList<>();
            for (Map<String, Object> childConnection : childConnections) {
                final Map<String, Object> prefixed = cloneMap(childConnection);
                ProjectsNodeGraphSupport.setConnectionFrom(prefixed, prefix + ProjectsNodeGraphSupport.connectionFrom(childConnection));
                ProjectsNodeGraphSupport.setConnectionTo(prefixed, prefix + ProjectsNodeGraphSupport.connectionTo(childConnection));
                prefixedChildConnections.add(prefixed);
            }

            final List<String> entryNodeIds = resolveEntryNodeIds(childWorkflow, childNodes, childConnections).stream()
                    .map(id -> prefix + id)
                    .toList();
            final List<String> exitNodeIds = resolveExitNodeIds(childWorkflow, childNodes, childConnections).stream()
                    .map(id -> prefix + id)
                    .toList();
            replacementByNodeId.put(nodeId, new ReplacementGraph(entryNodeIds, exitNodeIds));
            expandedNodes.addAll(prefixedChildNodes);
            expandedConnections.addAll(prefixedChildConnections);
            final Map<String, Object> childMetadata = new LinkedHashMap<>();
            childMetadata.put("embedded", true);
            childMetadata.put("depth", depth + 1);
            childMetadata.put("parentNodeId", nodeId);
            childMetadata.put("parentProjectId", currentProjectId != null ? currentProjectId : "");
            childMetadata.put("bindingSummary", summarizeBindingSummary(configuration));
            final Map<String, Object> callableContract = mapValue(configuration.get("_subWorkflowContract"));
            if (!callableContract.isEmpty()) {
                childMetadata.put("callableMode", optionalStringValue(callableContract.get("mode")));
                childMetadata.put("entrypointType",
                        optionalStringValue(mapValue(callableContract.get("entrypoint")).get("type")));
                childMetadata.put("version", optionalStringValue(callableContract.get("version")));
            }
            children.add(Map.of(
                    "childId", nodeId,
                    "projectId", resolvedChildProjectId != null ? resolvedChildProjectId : "",
                    "metadata", childMetadata));
        }

        if (!changed) {
            return specPayload;
        }

        final List<Map<String, Object>> rewrittenConnections = new ArrayList<>();
        for (Map<String, Object> connection : connections) {
            final String from = ProjectsNodeGraphSupport.connectionFrom(connection);
            final String to = ProjectsNodeGraphSupport.connectionTo(connection);
            final ReplacementGraph fromReplacement = replacementByNodeId.get(from);
            final ReplacementGraph toReplacement = replacementByNodeId.get(to);
            if (fromReplacement == null && toReplacement == null) {
                rewrittenConnections.add(cloneMap(connection));
                continue;
            }
            final List<String> sourceIds = fromReplacement != null ? fromReplacement.exitNodeIds : List.of(from);
            final List<String> targetIds = toReplacement != null ? toReplacement.entryNodeIds : List.of(to);
            for (String sourceId : sourceIds) {
                for (String targetId : targetIds) {
                    final Map<String, Object> bridge = cloneMap(connection);
                    ProjectsNodeGraphSupport.setConnectionFrom(bridge, sourceId);
                    ProjectsNodeGraphSupport.setConnectionTo(bridge, targetId);
                    rewrittenConnections.add(bridge);
                }
            }
        }
        rewrittenConnections.addAll(expandedConnections);

        workflow.put("nodes", expandedNodes);
        workflow.put("connections", rewrittenConnections);
        workflow.put("children", children);
        final Map<String, Object> metadata = mapValue(workflow.get("metadata"));
        metadata.put("maxSubWorkflowDepth", maxDepth);
        workflow.put("metadata", metadata);
        specPayload.put("workflow", workflow);
        return specPayload;
    }

    private static boolean isSubWorkflowNodeType(String type, Set<String> subWorkflowNodeTypes) {
        return type != null && subWorkflowNodeTypes.contains(type.trim().toLowerCase());
    }

    private static List<String> resolveEntryNodeIds(
            Map<String, Object> workflow,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> connections) {
        final String explicit = optionalStringValue(workflow.get("entryNodeId"));
        if (explicit != null) {
            return List.of(explicit);
        }
        final Set<String> referencedTargets = connections.stream()
                .map(ProjectsNodeGraphSupport::connectionTo)
                .collect(Collectors.toSet());
        return nodes.stream()
                .map(ProjectsNodeGraphSupport::resolveNodeId)
                .filter(id -> !referencedTargets.contains(id))
                .toList();
    }

    private static List<String> resolveExitNodeIds(
            Map<String, Object> workflow,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> connections) {
        final List<String> explicit = stringListValue(workflow.get("exitNodeIds"));
        if (!explicit.isEmpty()) {
            return explicit;
        }
        final Set<String> referencedSources = connections.stream()
                .map(ProjectsNodeGraphSupport::connectionFrom)
                .collect(Collectors.toSet());
        return nodes.stream()
                .map(ProjectsNodeGraphSupport::resolveNodeId)
                .filter(id -> !referencedSources.contains(id))
                .toList();
    }

    private static Map<String, Object> cloneMap(Map<String, Object> input) {
        return input != null ? new LinkedHashMap<>(input) : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            final Map<String, Object> result = new HashMap<>();
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
            return result;
        }
        return new HashMap<>();
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
            return new ArrayList<>();
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

    private static long longValue(Object raw, long fallback) {
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(raw).trim());
        } catch (Exception ignored) {
            return fallback;
        }
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

    private record ReplacementGraph(List<String> entryNodeIds, List<String> exitNodeIds) {
    }
}
