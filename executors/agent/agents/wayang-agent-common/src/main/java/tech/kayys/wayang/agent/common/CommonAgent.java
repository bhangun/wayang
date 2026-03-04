package tech.kayys.wayang.agent.common;

import tech.kayys.wayang.agent.AgentCapability;
import tech.kayys.wayang.agent.AgentCapabilityType;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

import java.util.Map;
import java.util.Set;

/**
 * Common Agent - General-purpose task execution
 */
public record CommonAgent(
        String specialization,
        Set<String> supportedTasks) implements AgentPlugin {

    @Override
    public String id() {
        return "common-agent";
    }

    @Override
    public String name() {
        return "Common Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "General-purpose agent for data processing and task execution";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.COMMON;
    }

    public Set<AgentCapability> requiredCapabilities() {
        return Set.of(
                new AgentCapability("Reasoning", "General reasoning capability", AgentCapabilityType.REASONING,
                        Map.of()),
                new AgentCapability("Tool Use", "Ability to use external tools", AgentCapabilityType.TOOL_USE,
                        Map.of()));
    }
}
