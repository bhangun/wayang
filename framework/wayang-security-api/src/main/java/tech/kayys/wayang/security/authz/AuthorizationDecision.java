package tech.kayys.wayang.security.authz;

import java.util.Map;

/**
 * Decision returned by the authorization service.
 * Obligations allow attaching runtime enforcement requirements (e.g. audit, rate-limit, HITL).
 */
public record AuthorizationDecision(
        boolean allowed,
        String reason,
        Map<String, Object> obligations
) {

    public AuthorizationDecision {
        obligations = obligations == null ? Map.of() : Map.copyOf(obligations);
    }

    public static AuthorizationDecision allow(String reason) {
        return new AuthorizationDecision(true, reason, Map.of());
    }

    public static AuthorizationDecision allow() {
        return allow("allowed");
    }

    public static AuthorizationDecision deny(String reason) {
        return new AuthorizationDecision(false, reason, Map.of());
    }
}
