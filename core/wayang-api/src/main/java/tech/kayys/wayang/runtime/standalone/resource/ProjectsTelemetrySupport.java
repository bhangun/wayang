package tech.kayys.wayang.runtime.standalone.resource;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ProjectsTelemetrySupport {
    private ProjectsTelemetrySupport() {
    }

    static Map<String, Object> aggregateTelemetry(
            String projectId,
            String executionId,
            List<Map<String, Object>> events,
            String from,
            String to,
            String nodeId,
            String type,
            String groupBy,
            String sort,
            int limit,
            boolean includeRaw) {
        final Map<String, Object> counters = new LinkedHashMap<>();
        counters.put("tasksExecuted", 0L);
        counters.put("delegationAttempts", 0L);
        counters.put("delegationRetries", 0L);
        counters.put("delegationFailures", 0L);
        counters.put("delegationTimeouts", 0L);

        final Set<String> orchestrationTypes = new HashSet<>();
        Map<String, Object> latestBudget = Map.of();
        Map<String, Object> executionContext = Map.of();
        int telemetryEventCount = 0;

        for (Map<String, Object> event : events) {
            final Map<String, Object> metadata = mapValue(event.get("metadata"));
            if (executionContext.isEmpty()) {
                final Map<String, Object> ctx = mapValue(metadata.get("executionContext"));
                if (!ctx.isEmpty()) {
                    executionContext = new LinkedHashMap<>(ctx);
                }
            }
            final Map<String, Object> telemetry = mapValue(metadata.get("telemetry"));
            if (telemetry.isEmpty()) {
                continue;
            }
            telemetryEventCount++;

            final String orchestrationType = optionalStringValue(telemetry.get("orchestrationType"));
            if (orchestrationType != null) {
                orchestrationTypes.add(orchestrationType);
            }
            counters.put("tasksExecuted",
                    ((Number) counters.get("tasksExecuted")).longValue() + longValue(telemetry.get("tasksExecuted"), 0L));

            final Map<String, Object> executorTelemetry = mapValue(telemetry.get("executorTelemetry"));
            if (!executorTelemetry.isEmpty()) {
                counters.put("delegationAttempts",
                        ((Number) counters.get("delegationAttempts")).longValue()
                                + longValue(executorTelemetry.get("delegationAttempts"), 0L));
                counters.put("delegationRetries",
                        ((Number) counters.get("delegationRetries")).longValue()
                                + longValue(executorTelemetry.get("delegationRetries"), 0L));
                counters.put("delegationFailures",
                        ((Number) counters.get("delegationFailures")).longValue()
                                + longValue(executorTelemetry.get("delegationFailures"), 0L));
                counters.put("delegationTimeouts",
                        ((Number) counters.get("delegationTimeouts")).longValue()
                                + longValue(executorTelemetry.get("delegationTimeouts"), 0L));
            }

            final Map<String, Object> budget = mapValue(telemetry.get("budget"));
            if (!budget.isEmpty()) {
                latestBudget = budget;
            }
        }

        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("projectId", projectId);
        response.put("executionId", executionId);
        response.put("eventCount", events.size());
        response.put("telemetryEventCount", telemetryEventCount);
        response.put("orchestrationTypes", orchestrationTypes.stream().sorted().toList());
        if (!executionContext.isEmpty()) {
            response.put("executionContext", executionContext);
        }
        final Map<String, Object> filters = new LinkedHashMap<>();
        if (optionalStringValue(from) != null) {
            filters.put("from", from);
        }
        if (optionalStringValue(to) != null) {
            filters.put("to", to);
        }
        if (nodeId != null) {
            filters.put("nodeId", nodeId);
        }
        if (type != null) {
            filters.put("type", type);
        }
        if (groupBy != null) {
            filters.put("groupBy", groupBy);
        }
        if (sort != null) {
            filters.put("sort", sort);
        }
        if (limit > 0) {
            filters.put("limit", limit);
        }
        filters.put("includeRaw", includeRaw);
        response.put("filters", filters);
        response.put("counters", counters);
        if (!latestBudget.isEmpty()) {
            response.put("latestBudget", latestBudget);
        }
        if ("nodeId".equalsIgnoreCase(groupBy) || "type".equalsIgnoreCase(groupBy)) {
            List<Map<String, Object>> grouped = "nodeId".equalsIgnoreCase(groupBy)
                    ? aggregateByNode(events)
                    : aggregateByType(events);
            grouped = sortGrouped(grouped, sort);
            if (limit > 0 && grouped.size() > limit) {
                grouped = grouped.subList(0, limit);
            }
            response.put("grouped", grouped);
        }
        if (includeRaw) {
            response.put("rawEventCount", events.size());
            response.put("rawEvents", events);
        }
        response.put("aggregatedAt", Instant.now().toString());
        return response;
    }

    static Instant parseFilterInstant(String value) {
        final String raw = optionalStringValue(value);
        if (raw == null) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    static boolean eventMatchesFilter(
            Map<String, Object> event,
            Instant from,
            Instant to,
            String nodeId,
            String type) {
        if (event == null) {
            return false;
        }
        final Instant createdAt = parseInstantOrEpoch(event.get("createdAt"));
        if (from != null && createdAt.isBefore(from)) {
            return false;
        }
        if (to != null && createdAt.isAfter(to)) {
            return false;
        }
        if (nodeId != null && !nodeId.equals(optionalStringValue(event.get("nodeId")))) {
            return false;
        }
        return type == null || type.equals(optionalStringValue(event.get("type")));
    }

    private static List<Map<String, Object>> aggregateByNode(List<Map<String, Object>> events) {
        final Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            if (event == null) {
                continue;
            }
            final String nodeId = optionalStringValue(event.get("nodeId"));
            if (nodeId == null) {
                continue;
            }
            final Map<String, Object> metadata = mapValue(event.get("metadata"));
            final Map<String, Object> telemetry = mapValue(metadata.get("telemetry"));
            if (telemetry.isEmpty()) {
                continue;
            }
            final Map<String, Object> counters = grouped.computeIfAbsent(nodeId, ignored -> defaultGroupedItem("nodeId", nodeId));
            incrementGroupedCounters(counters, telemetry);
        }
        return grouped.values().stream()
                .sorted(Comparator.comparing(entry -> String.valueOf(entry.getOrDefault("nodeId", ""))))
                .toList();
    }

    private static List<Map<String, Object>> aggregateByType(List<Map<String, Object>> events) {
        final Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            if (event == null) {
                continue;
            }
            final String type = optionalStringValue(event.get("type"));
            if (type == null) {
                continue;
            }
            final Map<String, Object> metadata = mapValue(event.get("metadata"));
            final Map<String, Object> telemetry = mapValue(metadata.get("telemetry"));
            if (telemetry.isEmpty()) {
                continue;
            }
            final Map<String, Object> counters = grouped.computeIfAbsent(type, ignored -> defaultGroupedItem("type", type));
            incrementGroupedCounters(counters, telemetry);
        }
        return grouped.values().stream()
                .sorted(Comparator.comparing(entry -> String.valueOf(entry.getOrDefault("type", ""))))
                .toList();
    }

    private static Map<String, Object> defaultGroupedItem(String key, String value) {
        final Map<String, Object> item = new LinkedHashMap<>();
        item.put(key, value);
        item.put("eventCount", 0L);
        item.put("tasksExecuted", 0L);
        item.put("delegationAttempts", 0L);
        item.put("delegationRetries", 0L);
        item.put("delegationFailures", 0L);
        item.put("delegationTimeouts", 0L);
        return item;
    }

    private static void incrementGroupedCounters(Map<String, Object> counters, Map<String, Object> telemetry) {
        counters.put("eventCount", ((Number) counters.get("eventCount")).longValue() + 1L);
        counters.put("tasksExecuted",
                ((Number) counters.get("tasksExecuted")).longValue() + longValue(telemetry.get("tasksExecuted"), 0L));
        final Map<String, Object> execTelemetry = mapValue(telemetry.get("executorTelemetry"));
        counters.put("delegationAttempts",
                ((Number) counters.get("delegationAttempts")).longValue() + longValue(execTelemetry.get("delegationAttempts"), 0L));
        counters.put("delegationRetries",
                ((Number) counters.get("delegationRetries")).longValue() + longValue(execTelemetry.get("delegationRetries"), 0L));
        counters.put("delegationFailures",
                ((Number) counters.get("delegationFailures")).longValue() + longValue(execTelemetry.get("delegationFailures"), 0L));
        counters.put("delegationTimeouts",
                ((Number) counters.get("delegationTimeouts")).longValue() + longValue(execTelemetry.get("delegationTimeouts"), 0L));
    }

    private static List<Map<String, Object>> sortGrouped(List<Map<String, Object>> grouped, String sort) {
        if (grouped == null || grouped.isEmpty() || sort == null) {
            return grouped;
        }
        final String[] parts = sort.split(":", 2);
        final String field = parts[0].trim();
        final boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());
        Comparator<Map<String, Object>> comparator = Comparator.comparing(item -> String.valueOf(item.getOrDefault(field, "")));
        if (isNumericGroupField(field)) {
            comparator = Comparator.comparingLong(item -> longValue(item.get(field), 0L));
        }
        if (desc) {
            comparator = comparator.reversed();
        }
        return grouped.stream().sorted(comparator).toList();
    }

    private static boolean isNumericGroupField(String field) {
        return Set.of(
                "eventCount",
                "tasksExecuted",
                "delegationAttempts",
                "delegationRetries",
                "delegationFailures",
                "delegationTimeouts").contains(field);
    }

    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            final Map<String, Object> normalized = new LinkedHashMap<>();
            map.forEach((key, item) -> normalized.put(String.valueOf(key), item));
            return normalized;
        }
        return Map.of();
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = String.valueOf(raw).trim();
        return value.isEmpty() ? null : value;
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

    private static Instant parseInstantOrEpoch(Object raw) {
        final String value = optionalStringValue(raw);
        if (value == null) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return Instant.EPOCH;
        }
    }
}
