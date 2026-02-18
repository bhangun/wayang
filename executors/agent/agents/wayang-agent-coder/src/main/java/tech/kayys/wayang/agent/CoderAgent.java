package tech.kayys.wayang.agent.type;

import java.util.Set;

import tech.kayys.wayang.agent.AgentCapability;
import tech.kayys.wayang.agent.AgentRole;
import tech.kayys.wayang.agent.code.CodeCapability;

/**
 * Coder Agent - Code generation and analysis
 */
public record CoderAgent(
        Set<String> programmingLanguages,
        Set<CodeCapability> codeCapabilities,
        String executionEnvironment) implements AgentType {

    @Override
    public String typeName() {
        return "CODER_AGENT";
    }

    @Override
    public Set<AgentCapability> requiredCapabilities() {
        return Set.of(
                AgentCapability.CODE_GENERATION,
                AgentCapability.CODE_ANALYSIS,
                AgentCapability.TOOL_USE);
    }

    @Override
    public AgentRole role() {
        return AgentRole.CODER;
    }
}
