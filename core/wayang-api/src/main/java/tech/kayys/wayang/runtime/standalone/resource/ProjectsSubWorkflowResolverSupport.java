package tech.kayys.wayang.runtime.standalone.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsSubWorkflowResolverSupport {
    private ProjectsSubWorkflowResolverSupport() {
    }

    static int resolveMaxSubWorkflowDepth(
            Map<String, Object> request,
            Map<String, Object> specPayload,
            int defaultMaxSubWorkflowDepth) {
        long requested = ProjectsExecutionLifecycleSupport.longValue(
                request != null ? request.get("maxSubWorkflowDepth") : null,
                -1L);
        if (requested <= 0) {
            final Map<String, Object> workflow = mapValue(specPayload.get("workflow"));
            final Map<String, Object> metadata = mapValue(workflow.get("metadata"));
            requested = ProjectsExecutionLifecycleSupport.longValue(
                    metadata.get("maxSubWorkflowDepth"),
                    defaultMaxSubWorkflowDepth);
        }
        if (requested <= 0) {
            requested = defaultMaxSubWorkflowDepth;
        }
        return (int) Math.min(5L, Math.max(1L, requested));
    }

    static Map<String, Object> expandSubWorkflowReferences(
            Map<String, Object> rawSpecPayload,
            String projectId,
            int maxDepth,
            String tenantId,
            String requesterUserId,
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
            Set<String> autonomousTriggerNodeTypes,
            Set<String> subWorkflowNodeTypes) throws IOException {
        return ProjectsSubWorkflowSupport.expandReferences(
                rawSpecPayload,
                projectId,
                maxDepth,
                tenantId,
                requesterUserId,
                subWorkflowNodeTypes,
                (childProjectId, requesterTenantId, requesterUser, parentNodeConfiguration, parentNodeId) ->
                        loadProjectSpecForSubWorkflow(
                                childProjectId,
                                requesterTenantId,
                                requesterUser,
                                parentNodeConfiguration,
                                parentNodeId,
                                defaultTenant,
                                defaultOwnerUser,
                                visibilityPrivate,
                                visibilityTenant,
                                visibilityExplicit,
                                visibilityPublic,
                                requiredPermissionExecuteSubworkflow,
                                reuseModeCallable,
                                reuseModeAutonomous,
                                entrypointManual,
                                entrypointParameterized,
                                entrypointEmpty,
                                callableEntrypointTypes,
                                manualStartNodeTypes,
                                parameterizedStartNodeTypes,
                                autonomousTriggerNodeTypes));
    }

    private static Map<String, Object> loadProjectSpecForSubWorkflow(
            String projectId,
            String requesterTenantId,
            String requesterUserId,
            Map<String, Object> parentNodeConfiguration,
            String parentNodeId,
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
            Set<String> autonomousTriggerNodeTypes) throws IOException {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("Sub-workflow node requires 'projectId' or inline 'wayangSpec/spec'");
        }
        final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
        final Map<String, Object> project = projects.stream()
                .filter(item -> projectId.equals(String.valueOf(item.get("projectId"))))
                .findFirst()
                .orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("Sub-workflow project not found: " + projectId);
        }
        ProjectsCallableSupport.ensureSubWorkflowAccess(
                projectId,
                project,
                requesterTenantId,
                requesterUserId,
                defaultTenant,
                defaultOwnerUser,
                visibilityPrivate,
                visibilityTenant,
                visibilityExplicit,
                visibilityPublic,
                requiredPermissionExecuteSubworkflow);
        final Map<String, Object> callableContract = ProjectsCallableSupport.ensureCallableSubWorkflowContract(
                projectId,
                project,
                parentNodeConfiguration,
                parentNodeId,
                reuseModeCallable,
                reuseModeAutonomous,
                entrypointManual,
                entrypointParameterized,
                entrypointEmpty,
                callableEntrypointTypes,
                manualStartNodeTypes,
                parameterizedStartNodeTypes,
                autonomousTriggerNodeTypes);
        if (!callableContract.isEmpty()) {
            parentNodeConfiguration.put("_subWorkflowContract", callableContract);
        }
        final Map<String, Object> metadata = mapValue(project.get("metadata"));
        final Object rawWayangSpec = firstNonNull(
                metadata.get("wayangSpec"),
                metadata.get("spec"),
                mapValue(metadata.get("wayangProject")).get("wayangSpec"),
                mapValue(metadata.get("wayangProject")).get("spec"));
        if (rawWayangSpec == null) {
            throw new IllegalArgumentException(
                    "Project '" + projectId + "' has no saved spec in metadata.wayangSpec/metadata.spec");
        }
        return ProjectsSubWorkflowSupport.normalizeAsWayangSpec(rawWayangSpec);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            final Map<String, Object> result = new HashMap<>();
            rawMap.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return new HashMap<>();
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
}
