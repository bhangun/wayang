package tech.kayys.wayang.control.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.agent.AgentStatus;
import tech.kayys.wayang.control.domain.AIAgent;
import tech.kayys.wayang.control.domain.WayangProject;
import tech.kayys.wayang.control.dto.AgentExecutionResult;
import tech.kayys.wayang.control.dto.AgentTask;
import tech.kayys.wayang.control.dto.CreateAgentRequest;
import tech.kayys.wayang.control.spi.AgentManagerSpi;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing AI Agents.
 */
@ApplicationScoped
public class AgentManager implements AgentManagerSpi {

    private static final Logger LOG = LoggerFactory.getLogger(AgentManager.class);

    /**
     * Create a new AI Agent.
     */
    public Uni<AIAgent> createAgent(UUID projectId, CreateAgentRequest request) {
        LOG.info("Creating AI agent: {} in project: {}", request.agentName(), projectId);

        return Panache.withTransaction(() -> WayangProject.<WayangProject>findById(projectId)
                .flatMap(project -> {
                    if (project == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Project not found"));
                    }

                    AIAgent agent = new AIAgent();
                    agent.project = project;
                    agent.agentName = request.agentName();
                    agent.description = request.description();
                    agent.agentType = request.agentType();
                    agent.tools = request.tools();
                    agent.capabilities = request.capabilities();
                    agent.llmConfig = request.llmConfig();
                    agent.memoryConfig = request.memoryConfig();
                    agent.guardrails = request.guardrails();
                    agent.status = AgentStatus.AVAILABLE;
                    agent.createdAt = Instant.now();

                    return agent.persist().map(a -> (AIAgent) a);
                }));
    }

    /**
     * Get an agent by ID.
     */
    public Uni<AIAgent> getAgent(UUID agentId) {
        return AIAgent.findById(agentId);
    }

    /**
     * Activate an agent.
     */
    public Uni<AIAgent> activateAgent(UUID agentId) {
        LOG.info("Activating agent: {}", agentId);
        return Panache.withTransaction(() -> AIAgent.<AIAgent>findById(agentId)
                .flatMap(agent -> {
                    if (agent == null)
                        return Uni.createFrom().nullItem();
                    // agent.status = AgentStatus.ACTIVE; // Need to ensure status field exists in
                    // entity
                    return agent.persist();
                }));
    }

    /**
     * Deactivate an agent.
     */
    public Uni<AIAgent> deactivateAgent(UUID agentId) {
        LOG.info("Deactivating agent: {}", agentId);
        return Panache.withTransaction(() -> AIAgent.<AIAgent>findById(agentId)
                .flatMap(agent -> {
                    if (agent == null)
                        return Uni.createFrom().nullItem();
                    // agent.status = AgentStatus.INACTIVE;
                    return agent.persist();
                }));
    }

    /**
     * Execute a task with an agent.
     */
    public Uni<AgentExecutionResult> executeTask(UUID agentId, AgentTask task) {
        LOG.warn("Agent execution is currently stubbed");
        return Uni.createFrom().item(new AgentExecutionResult(
                task.taskId() != null ? task.taskId() : UUID.randomUUID().toString(),
                false,
                "Execution stubbed: Orchestrator unavailable",
                java.util.Collections.emptyList(),
                java.util.Collections.emptyMap(),
                java.util.List.of("Orchestrator unavailable")));
    }
}
