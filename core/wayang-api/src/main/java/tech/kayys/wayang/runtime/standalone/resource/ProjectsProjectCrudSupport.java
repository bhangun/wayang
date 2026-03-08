package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class ProjectsProjectCrudSupport {
    private ProjectsProjectCrudSupport() {
    }

    static List<Map<String, Object>> listProjects() {
        try {
            return ProjectsFileStore.readProjects();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    static Response createProject(
            Map<String, Object> request,
            String defaultTenant,
            String defaultOwnerUser,
            String defaultType,
            String visibilityPrivate,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        try {
            final Map<String, Object> body = request != null ? request : Map.of();
            final String now = Instant.now().toString();
            final String tenantId = stringValue(body.get("tenantId"), defaultTenant);
            final String createdBy = stringValue(body.get("createdBy"), defaultOwnerUser);

            final Map<String, Object> project = new LinkedHashMap<>();
            project.put("projectId", UUID.randomUUID().toString());
            project.put("tenantId", tenantId);
            project.put("projectName", stringValue(body.get("projectName"), "Wayang Project"));
            project.put("description", stringValue(body.get("description"), ""));
            project.put("projectType", stringValue(body.get("projectType"), defaultType));
            project.put("createdAt", now);
            project.put("updatedAt", now);
            project.put("savedAt", now);
            project.put("createdBy", createdBy);
            project.put("source", "server");

            final Map<String, Object> metadata = mapValue(body.get("metadata"));
            project.put("metadata", withDefaultProjectMetadata(
                    metadata,
                    tenantId,
                    createdBy,
                    visibilityPrivate,
                    reuseModeCallable,
                    reuseModeAutonomous,
                    entrypointManual,
                    entrypointParameterized,
                    entrypointEmpty,
                    manualStartNodeTypes,
                    parameterizedStartNodeTypes,
                    autonomousTriggerNodeTypes));

            final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
            projects.removeIf(existing -> project.get("projectId").equals(existing.get("projectId")));
            projects.add(project);
            ProjectsFileStore.writeProjects(projects);

            return Response.status(Response.Status.CREATED).entity(project).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to persist project", "message", e.getMessage()))
                    .build();
        }
    }

    static Response getProject(String projectId) {
        try {
            final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
            return projects.stream()
                    .filter(p -> projectId.equals(String.valueOf(p.get("projectId"))))
                    .findFirst()
                    .<Response>map(p -> Response.ok(p).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read project", "message", e.getMessage()))
                    .build();
        }
    }

    static Response updateProject(
            String projectId,
            Map<String, Object> request,
            String defaultTenant,
            String defaultOwnerUser,
            String defaultType,
            String visibilityPrivate,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        try {
            final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
            Map<String, Object> target = null;
            for (Map<String, Object> project : projects) {
                if (projectId.equals(String.valueOf(project.get("projectId")))) {
                    target = project;
                    break;
                }
            }
            if (target == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            target.put("projectName", stringValue(body.get("projectName"), stringValue(target.get("projectName"), "Wayang Project")));
            target.put("description", stringValue(body.get("description"), stringValue(target.get("description"), "")));
            target.put("projectType", stringValue(body.get("projectType"), stringValue(target.get("projectType"), defaultType)));
            final String tenantId = stringValue(body.get("tenantId"), stringValue(target.get("tenantId"), defaultTenant));
            target.put("tenantId", tenantId);
            final String now = Instant.now().toString();
            target.put("updatedAt", now);
            target.put("savedAt", now);
            target.put("source", "server");

            final Map<String, Object> incomingMetadata = mapValue(body.get("metadata"));
            if (!incomingMetadata.isEmpty()) {
                final String createdBy = stringValue(target.get("createdBy"), defaultOwnerUser);
                target.put("metadata", withDefaultProjectMetadata(
                        incomingMetadata,
                        tenantId,
                        createdBy,
                        visibilityPrivate,
                        reuseModeCallable,
                        reuseModeAutonomous,
                        entrypointManual,
                        entrypointParameterized,
                        entrypointEmpty,
                        manualStartNodeTypes,
                        parameterizedStartNodeTypes,
                        autonomousTriggerNodeTypes));
            }

            ProjectsFileStore.writeProjects(projects);
            return Response.ok(target).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to update project", "message", e.getMessage()))
                    .build();
        }
    }

    static Response deleteProject(String projectId) {
        try {
            final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
            final int before = projects.size();
            projects.removeIf(project -> projectId.equals(String.valueOf(project.get("projectId"))));
            if (projects.size() == before) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ProjectsFileStore.writeProjects(projects);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to delete project", "message", e.getMessage()))
                    .build();
        }
    }

    private static Map<String, Object> withDefaultProjectMetadata(
            Map<String, Object> metadataRaw,
            String ownerTenantId,
            String ownerUserId,
            String visibilityPrivate,
            String reuseModeCallable,
            String reuseModeAutonomous,
            String entrypointManual,
            String entrypointParameterized,
            String entrypointEmpty,
            Set<String> manualStartNodeTypes,
            Set<String> parameterizedStartNodeTypes,
            Set<String> autonomousTriggerNodeTypes) {
        final Map<String, Object> metadata = withDefaultAccessPolicy(
                metadataRaw, ownerTenantId, ownerUserId, visibilityPrivate);
        if (!metadata.containsKey("reuse")) {
            final Map<String, Object> inferred = ProjectsCallableSupport.resolveCallableContract(
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
            final Map<String, Object> reuse = new LinkedHashMap<>();
            reuse.put("enabled", booleanValue(inferred.get("enabled")));
            reuse.put("mode", stringValue(inferred.get("mode"), reuseModeAutonomous));
            final String version = optionalStringValue(inferred.get("version"));
            if (version != null) {
                reuse.put("version", version);
            }
            reuse.put("entrypoint", mapValue(inferred.get("entrypoint")));
            reuse.put("contract", Map.of(
                    "inputs", mapValue(inferred.get("inputs")),
                    "output", mapValue(inferred.get("output"))));
            metadata.put("reuse", reuse);
        }
        return metadata;
    }

    private static Map<String, Object> withDefaultAccessPolicy(
            Map<String, Object> metadataRaw,
            String ownerTenantId,
            String ownerUserId,
            String visibilityPrivate) {
        final Map<String, Object> metadata = new LinkedHashMap<>(metadataRaw != null ? metadataRaw : Map.of());
        final Map<String, Object> access = new LinkedHashMap<>(mapValue(metadata.get("access")));
        access.put("ownerTenantId", stringValue(access.get("ownerTenantId"), ownerTenantId));
        access.put("ownerUserId", stringValue(access.get("ownerUserId"), ownerUserId));
        access.put("visibility", stringValue(access.get("visibility"), visibilityPrivate).toLowerCase());
        if (!access.containsKey("sharedWithUsers")) {
            access.put("sharedWithUsers", List.of());
        }
        if (!access.containsKey("sharedWithTenants")) {
            access.put("sharedWithTenants", List.of());
        }
        if (!access.containsKey("requireConsent")) {
            access.put("requireConsent", false);
        }
        metadata.put("access", access);
        return metadata;
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

    private static boolean booleanValue(Object raw) {
        if (raw == null) {
            return false;
        }
        if (raw instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (raw instanceof Number numeric) {
            return numeric.intValue() != 0;
        }
        final String value = raw.toString().trim().toLowerCase();
        return "true".equals(value) || "1".equals(value) || "yes".equals(value) || "y".equals(value);
    }
}
