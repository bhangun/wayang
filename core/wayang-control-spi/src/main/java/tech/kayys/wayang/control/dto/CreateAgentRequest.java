package tech.kayys.wayang.control.dto;

import java.util.List;

import tech.kayys.wayang.agent.AgentType;

public record CreateAgentRequest(
        String agentName,
        String description,
        AgentType agentType,
        LLMConfig llmConfig,
        List<AgentCapability> capabilities,
        List<AgentTool> tools,
        MemoryConfig memoryConfig,
        List<Guardrail> guardrails) {
}
