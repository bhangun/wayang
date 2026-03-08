package tech.kayys.wayang.runtime.standalone.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsLineageSupport {
    static final List<String> TRACE_FIELD_ORDER = List.of(
            "projectId",
            "childProjectId",
            "childId",
            "parentNodeId",
            "parentProjectId",
            "depth",
            "invokeMode",
            "waitForCompletion",
            "bindingSummary");
    static final Set<String> TRACE_FIELDS = Set.copyOf(TRACE_FIELD_ORDER);
    static final List<String> INCLUDE_FIELD_ORDER = List.of(
            "executionContext",
            "subWorkflowResolution",
            "status",
            "updatedAt");
    static final Set<String> INCLUDE_FIELDS = Set.copyOf(INCLUDE_FIELD_ORDER);

    private ProjectsLineageSupport() {
    }

    static List<Map<String, Object>> sortTrace(List<Map<String, Object>> trace, String sort) {
        if (trace == null || trace.isEmpty() || sort == null || sort.isBlank()) {
            return trace != null ? trace : List.of();
        }
        final String[] parts = sort.split(":", 2);
        final String field = parts[0].trim();
        final boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());
        Comparator<Map<String, Object>> comparator;
        if ("depth".equals(field)) {
            comparator = Comparator.comparingLong(item -> longValue(item.get("depth"), 0L));
        } else {
            comparator = Comparator.comparing(item -> String.valueOf(item.getOrDefault(field, "")));
        }
        if (desc) {
            comparator = comparator.reversed();
        }
        return trace.stream().sorted(comparator).toList();
    }

    static List<String> parseRequestedValues(String rawValues) {
        final String raw = optionalStringValue(rawValues);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    static List<Map<String, Object>> projectTrace(List<Map<String, Object>> trace, List<String> selectedFields) {
        if (trace == null || trace.isEmpty() || selectedFields == null || selectedFields.isEmpty()) {
            return trace != null ? trace : List.of();
        }
        final List<Map<String, Object>> projected = new ArrayList<>(trace.size());
        for (Map<String, Object> item : trace) {
            final Map<String, Object> projectedItem = new LinkedHashMap<>();
            for (String field : selectedFields) {
                if (item.containsKey(field)) {
                    projectedItem.put(field, item.get(field));
                }
            }
            projected.add(projectedItem);
        }
        return projected;
    }

    static List<String> orderedAcceptedValues(List<String> requestedValues, List<String> canonicalOrder) {
        if (requestedValues == null || requestedValues.isEmpty() || canonicalOrder == null || canonicalOrder.isEmpty()) {
            return List.of();
        }
        final Set<String> requestedSet = Set.copyOf(requestedValues);
        return canonicalOrder.stream()
                .filter(requestedSet::contains)
                .toList();
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
}
