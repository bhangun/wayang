package tech.kayys.wayang.agent.dto;

import java.time.Instant;

import tech.kayys.gollek.spi.provider.LLMProvider;
import tech.kayys.wayang.memory.spi.AgentMemory;

/**
 * Active agent runtime
 */
public class ActiveAgent {
    public final AIAgent agent;
    public final LLMProvider llmProvider;
    public final AgentMemory memory;
    public final Instant activatedAt;

    public ActiveAgent(AIAgent agent, LLMProvider llmProvider, AgentMemory memory) {
        this.agent = agent;
        this.llmProvider = llmProvider;
        this.memory = memory;
        this.activatedAt = Instant.now();
    }
}
