package tech.kayys.wayang.agent;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Agent Registration - Metadata for agent discovery.
 */
public record AgentRegistration(
        String agentId,
        String agentName,
        AgentType agentType,
        Set<AgentCapability> capabilities,
        AgentStatus status,
        AgentEndpoint endpoint,
        Map<String, String> metadata,
        Instant registeredAt,
        Instant lastHeartbeat) {

    public boolean isAvailable() {
        return status == AgentStatus.AVAILABLE || status == AgentStatus.ACTIVE;
    }

    public boolean isHealthy() {
        if (lastHeartbeat == null)
            return false;
        return lastHeartbeat.isAfter(Instant.now().minusSeconds(30));
    }
}
