package tech.kayys.wayang.runtime.standalone.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class ProjectsNodeGraphSupport {
    private ProjectsNodeGraphSupport() {
    }

    static String resolveNodeId(Map<String, Object> node, int fallbackIndex) {
        final String id = optionalStringValue(node.get("id"));
        if (id != null) {
            return id;
        }
        final String metadataId = optionalStringValue(mapValue(node.get("metadata")).get("id"));
        if (metadataId != null) {
            return metadataId;
        }
        final String generated = "node-" + (fallbackIndex >= 0 ? fallbackIndex : UUID.randomUUID());
        setNodeId(node, generated);
        return generated;
    }

    static String resolveNodeId(Map<String, Object> node) {
        return resolveNodeId(node, -1);
    }

    static void setNodeId(Map<String, Object> node, String id) {
        if (node == null || id == null || id.isBlank()) {
            return;
        }
        node.remove("id");
        final Map<String, Object> metadata = mapValue(node.get("metadata"));
        metadata.put("id", id);
        node.put("metadata", metadata);
    }

    static Map<String, Object> mergedNodeConfiguration(Map<String, Object> node) {
        final Map<String, Object> configuration = mapValue(node.get("configuration"));
        final Map<String, Object> legacyConfig = mapValue(node.get("config"));
        if (!legacyConfig.isEmpty()) {
            configuration.putAll(legacyConfig);
        }
        return configuration;
    }

    static String connectionFrom(Map<String, Object> connection) {
        return firstNonBlank(
                optionalStringValue(connection.get("fromNodeId")),
                optionalStringValue(connection.get("from")),
                "");
    }

    static String connectionTo(Map<String, Object> connection) {
        return firstNonBlank(
                optionalStringValue(connection.get("toNodeId")),
                optionalStringValue(connection.get("to")),
                "");
    }

    static void setConnectionFrom(Map<String, Object> connection, String from) {
        connection.put("fromNodeId", from);
        connection.remove("from");
    }

    static void setConnectionTo(Map<String, Object> connection, String to) {
        connection.put("toNodeId", to);
        connection.remove("to");
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

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
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
}
