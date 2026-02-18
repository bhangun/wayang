package tech.kayys.wayang.eip.config;

import java.util.Map;

public record AuthConfig(String type, String credential) {
    @SuppressWarnings("unchecked")
    public static AuthConfig fromContext(Map<String, Object> context) {
        Map<String, Object> auth = (Map<String, Object>) context.get("auth");
        if (auth == null)
            return new AuthConfig("none", null);
        return new AuthConfig(
                (String) auth.getOrDefault("type", "none"),
                (String) auth.get("credential"));
    }
}
