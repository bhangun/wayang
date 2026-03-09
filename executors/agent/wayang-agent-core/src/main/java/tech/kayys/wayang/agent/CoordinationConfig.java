package tech.kayys.wayang.agent;

import java.util.Map;

/**
 * Coordination Configuration
 */
public record CoordinationConfig(
        long timeoutMs,
        int minParticipants,
        Map<String, Object> params) {
}
