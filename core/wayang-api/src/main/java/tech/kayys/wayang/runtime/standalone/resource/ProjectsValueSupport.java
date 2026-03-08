package tech.kayys.wayang.runtime.standalone.resource;

import java.util.HashMap;
import java.util.Map;

final class ProjectsValueSupport {
    private ProjectsValueSupport() {
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            final Map<String, Object> result = new HashMap<>();
            rawMap.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return new HashMap<>();
    }

    static String stringValue(Object raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    static boolean booleanValue(Object raw) {
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

    static String firstNonBlank(String... values) {
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
}
