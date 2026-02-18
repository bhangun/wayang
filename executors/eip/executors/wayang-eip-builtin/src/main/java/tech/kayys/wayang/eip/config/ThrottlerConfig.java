package tech.kayys.wayang.eip.config;

import java.util.Map;

public record ThrottlerConfig(int rate, String nodeType) {
    public static ThrottlerConfig fromContext(Map<String, Object> context) {
        return new ThrottlerConfig(
                (Integer) context.getOrDefault("rate", 10),
                (String) context.getOrDefault("nodeType", "throttler"));
    }
}
