package tech.kayys.silat.model;

import java.util.Map;

/**
 * Executor Information
 */
public record ExecutorInfo(
        String executorId,
        String executorType,
        CommunicationType communicationType,
        String endpoint,
        Map<String, String> metadata) {
}
