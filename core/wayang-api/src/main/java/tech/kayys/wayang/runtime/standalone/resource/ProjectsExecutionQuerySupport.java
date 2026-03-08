package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WayangDefinitionService;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class ProjectsExecutionQuerySupport {
    private ProjectsExecutionQuerySupport() {
    }

    static Response listExecutions(String projectId) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            final List<Map<String, Object>> result = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId"))))
                    .sorted(Comparator.comparing((Map<String, Object> e) -> String.valueOf(e.getOrDefault("createdAt", "")))
                            .reversed())
                    .toList();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read executions", "message", e.getMessage()))
                    .build();
        }
    }

    static Response getExecutionStatus(
            String projectId,
            String executionId,
            String ifNoneMatch,
            Consumer<Map<String, Object>> latestStatusUpdater,
            String previousStatus,
            String latestStatus,
            boolean statusTransitionAllowed) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }

            String effectiveStatus = latestStatus;
            if (!statusTransitionAllowed) {
                effectiveStatus = previousStatus;
                ProjectsFileStore.appendExecutionEvent(
                        projectId,
                        executionId,
                        "EXECUTION_STATUS_TRANSITION_REJECTED",
                        previousStatus,
                        "Rejected invalid execution status transition from " + previousStatus + " to " + latestStatus,
                        Map.of(
                                "previousStatus", previousStatus,
                                "attemptedStatus", latestStatus));
            }
            execution.put("status", effectiveStatus);
            execution.put("updatedAt", Instant.now().toString());
            execution.putIfAbsent("version", 1L);
            if (latestStatusUpdater != null) {
                latestStatusUpdater.accept(execution);
            }
            ProjectsFileStore.writeExecutions(executions);
            final String resolvedStatus = effectiveStatus;
            if (!resolvedStatus.equalsIgnoreCase(previousStatus)) {
                ProjectsFileStore.appendExecutionEvent(
                        projectId,
                        executionId,
                        "EXECUTION_STATUS_CHANGED",
                        resolvedStatus,
                        "Execution status changed from " + previousStatus + " to " + resolvedStatus,
                        Map.of(
                                "previousStatus", previousStatus,
                                "currentStatus", resolvedStatus));
                ProjectsExecutionLifecycleSupport.bumpExecutionVersion(execution);
                ProjectsFileStore.writeExecutions(executions);
            }

            final String etag = ProjectsExecutionLifecycleSupport.executionVersionEtag(execution);
            if (ProjectsExecutionLifecycleSupport.etagEquals(ifNoneMatch, etag)) {
                return Response.notModified().tag(etag).build();
            }
            return Response.ok(execution).tag(etag).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution status", "message", e.getMessage()))
                    .build();
        }
    }

    static Response refreshAndGetExecutionStatus(
            String projectId,
            String executionId,
            String ifNoneMatch,
            WayangDefinitionService definitionService,
            String statusUnknown) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            final Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }
            final String previousStatus = ProjectsExecutionStatusSupport.normalizeStatus(
                    execution.getOrDefault("status", statusUnknown),
                    statusUnknown);
            final String latestStatus = ProjectsExecutionStatusSupport.normalizeStatus(
                    definitionService.getExecutionStatus(executionId).await().indefinitely(),
                    statusUnknown);
            return getExecutionStatus(
                    projectId,
                    executionId,
                    ifNoneMatch,
                    null,
                    previousStatus,
                    latestStatus,
                    ProjectsExecutionStatusSupport.isStatusTransitionAllowed(previousStatus, latestStatus, statusUnknown));
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution status", "message", e.getMessage()))
                    .build();
        }
    }

    static Response listExecutionEvents(String projectId, String executionId) {
        try {
            final List<Map<String, Object>> events = ProjectsFileStore.readExecutionEvents().stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .sorted(Comparator.comparing(e -> String.valueOf(e.getOrDefault("createdAt", ""))))
                    .toList();
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution events", "message", e.getMessage()))
                    .build();
        }
    }

    static Response getExecutionTelemetry(
            String projectId,
            String executionId,
            String from,
            String to,
            String nodeId,
            String type,
            String groupBy,
            String sort,
            Integer limit,
            boolean includeRaw) {
        try {
            final java.time.Instant fromInstant = ProjectsTelemetrySupport.parseFilterInstant(from);
            final java.time.Instant toInstant = ProjectsTelemetrySupport.parseFilterInstant(to);
            final String nodeFilter = optionalStringValue(nodeId);
            final String typeFilter = optionalStringValue(type);
            final String groupByValue = optionalStringValue(groupBy);
            final String sortValue = optionalStringValue(sort);
            final int limitValue = limit != null ? Math.max(0, limit) : 0;
            final List<Map<String, Object>> events = ProjectsFileStore.readExecutionEvents().stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .filter(e -> ProjectsTelemetrySupport.eventMatchesFilter(e, fromInstant, toInstant, nodeFilter, typeFilter))
                    .sorted(Comparator.comparing(e -> String.valueOf(e.getOrDefault("createdAt", ""))))
                    .toList();
            return Response.ok(ProjectsTelemetrySupport.aggregateTelemetry(
                    projectId, executionId, events, from, to, nodeFilter, typeFilter,
                    groupByValue, sortValue, limitValue, includeRaw))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution telemetry", "message", e.getMessage()))
                    .build();
        }
    }

    static Response getExecutionLineage(
            String projectId,
            String executionId,
            String view,
            String nodeId,
            String sort,
            Integer limit,
            Integer offset,
            String fields,
            String include) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            final Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }

            Map<String, Object> subWorkflowResolution = mapValue(execution.get("subWorkflowResolution"));
            if (subWorkflowResolution.isEmpty()) {
                final List<Map<String, Object>> events = ProjectsFileStore.readExecutionEvents().stream()
                        .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                                && executionId.equals(String.valueOf(e.get("executionId"))))
                        .sorted(Comparator.comparing(e -> String.valueOf(e.getOrDefault("createdAt", ""))))
                        .toList();
                for (Map<String, Object> event : events) {
                    final Map<String, Object> metadata = mapValue(event.get("metadata"));
                    final Map<String, Object> candidate = mapValue(metadata.get("subWorkflowResolution"));
                    if (!candidate.isEmpty()) {
                        subWorkflowResolution = candidate;
                        break;
                    }
                }
            }

            final List<Map<String, Object>> rawTrace = mapListValue(subWorkflowResolution.get("trace"));
            final String nodeFilter = optionalStringValue(nodeId);
            final List<Map<String, Object>> trace = rawTrace.stream()
                    .filter(item -> {
                        if (nodeFilter == null) {
                            return true;
                        }
                        final String parentNodeId = optionalStringValue(item.get("parentNodeId"));
                        final String childId = optionalStringValue(item.get("childId"));
                        return nodeFilter.equals(parentNodeId) || nodeFilter.equals(childId);
                    })
                    .toList();
            final List<Map<String, Object>> sortedTrace = ProjectsLineageSupport.sortTrace(trace, sort);
            final int safeOffset = Math.max(0, offset != null ? offset : 0);
            final int safeLimit = limit != null ? Math.max(0, limit) : 0;
            final int fromIndex = Math.min(safeOffset, sortedTrace.size());
            final int toIndex = safeLimit > 0
                    ? Math.min(sortedTrace.size(), fromIndex + safeLimit)
                    : sortedTrace.size();
            final List<Map<String, Object>> pagedTrace = sortedTrace.subList(fromIndex, toIndex);
            final List<String> requestedFields = ProjectsLineageSupport.parseRequestedValues(fields);
            final List<String> acceptedFields = ProjectsLineageSupport.orderedAcceptedValues(
                    requestedFields, ProjectsLineageSupport.TRACE_FIELD_ORDER);
            final List<String> ignoredFields = requestedFields.stream()
                    .filter(field -> !ProjectsLineageSupport.TRACE_FIELDS.contains(field))
                    .toList();
            final List<Map<String, Object>> projectedTrace = ProjectsLineageSupport.projectTrace(pagedTrace, acceptedFields);

            final String viewMode = stringValue(view, "full").toLowerCase();
            final boolean compact = "compact".equals(viewMode);
            final List<String> requestedIncludes = ProjectsLineageSupport.parseRequestedValues(include);
            final List<String> defaultIncludes = compact
                    ? List.of("executionContext")
                    : List.of("executionContext", "subWorkflowResolution", "status", "updatedAt");
            final List<String> acceptedIncludes = requestedIncludes.isEmpty()
                    ? defaultIncludes
                    : ProjectsLineageSupport.orderedAcceptedValues(
                            requestedIncludes, ProjectsLineageSupport.INCLUDE_FIELD_ORDER);
            final List<String> ignoredIncludes = requestedIncludes.stream()
                    .filter(field -> !ProjectsLineageSupport.INCLUDE_FIELDS.contains(field))
                    .toList();
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("projectId", projectId);
            payload.put("executionId", executionId);
            payload.put("view", compact ? "compact" : "full");
            payload.put("nodeId", nodeFilter);
            payload.put("sort", optionalStringValue(sort));
            payload.put("limit", safeLimit);
            payload.put("offset", safeOffset);
            payload.put("fields", acceptedFields);
            payload.put("ignoredFields", ignoredFields);
            payload.put("include", acceptedIncludes);
            payload.put("ignoredIncludes", ignoredIncludes);
            payload.put("traceCount", projectedTrace.size());
            payload.put("totalTraceCount", rawTrace.size());
            payload.put("filteredTraceCount", sortedTrace.size());
            payload.put("trace", projectedTrace);
            if (acceptedIncludes.contains("executionContext")) {
                payload.put("executionContext", mapValue(execution.get("executionContext")));
            }
            if (acceptedIncludes.contains("subWorkflowResolution")) {
                payload.put("subWorkflowResolution", subWorkflowResolution);
            }
            if (acceptedIncludes.contains("status")) {
                payload.put("status", optionalStringValue(execution.get("status")));
            }
            if (acceptedIncludes.contains("updatedAt")) {
                payload.put("updatedAt", optionalStringValue(execution.get("updatedAt")));
            }
            return Response.ok(payload).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution lineage", "message", e.getMessage()))
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

    private static List<Map<String, Object>> mapListValue(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(ProjectsExecutionQuerySupport::mapValue).toList();
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static String stringValue(Object raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? fallback : value;
    }
}
