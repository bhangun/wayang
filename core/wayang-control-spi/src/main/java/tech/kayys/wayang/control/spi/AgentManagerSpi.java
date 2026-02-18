package tech.kayys.wayang.control.spi;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.agent.AgentExecutionResult;
import tech.kayys.wayang.agent.AgentTask;
import tech.kayys.wayang.control.domain.AIAgent;
import tech.kayys.wayang.control.dto.CreateAgentRequest;

import java.util.UUID;

/**
 * SPI interface for agent management services.
 */
public interface AgentManagerSpi {
    
    /**
     * Create a new AI Agent.
     */
    Uni<AIAgent> createAgent(UUID projectId, CreateAgentRequest request);
    
    /**
     * Get an agent by ID.
     */
    Uni<AIAgent> getAgent(UUID agentId);
    
    /**
     * Activate an agent.
     */
    Uni<AIAgent> activateAgent(UUID agentId);
    
    /**
     * Deactivate an agent.
     */
    Uni<AIAgent> deactivateAgent(UUID agentId);
    
    /**
     * Execute a task with an agent.
     */
    Uni<AgentExecutionResult> executeTask(UUID agentId, AgentTask task);
}