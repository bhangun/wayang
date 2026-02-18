package tech.kayys.wayang.node.websearch.spi;

import java.util.Map;

public record ProviderConfig(String providerId, Map<String, String> properties) {
    public static ProviderConfig forProvider(String providerId) {
        return new ProviderConfig(providerId, Map.of());
    }

    public static ProviderConfig forProvider(String providerId, Map<String, String> properties) {
        return new ProviderConfig(providerId, properties == null ? Map.of() : Map.copyOf(properties));
    }

    public String get(String key) {
        return properties.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = properties.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
