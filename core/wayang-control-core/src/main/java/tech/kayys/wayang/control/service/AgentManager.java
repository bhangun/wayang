package tech.kayys.wayang.control.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.agent.AgentExecutionResult;
import tech.kayys.wayang.agent.AgentTask;
import tech.kayys.wayang.agent.AgentCapabilityType;
import tech.kayys.wayang.agent.AgentStatus;
import tech.kayys.wayang.control.domain.AIAgent;
import tech.kayys.wayang.control.domain.WayangProject;
import tech.kayys.wayang.control.dto.AgentCapability;
import tech.kayys.wayang.control.dto.CapabilityType;
import tech.kayys.wayang.control.dto.ToolType;
import tech.kayys.wayang.control.dto.CreateAgentRequest;
import tech.kayys.wayang.control.spi.AgentManagerSpi;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
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
                    agent.tools = mapTools(request.tools());
                    agent.capabilities = mapCapabilities(request.capabilities());
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

    private List<tech.kayys.wayang.control.dto.AgentTool> mapTools(List<tech.kayys.wayang.agent.AgentTool> tools) {
        if (tools == null) {
            return null;
        }

        return tools.stream().map(tool -> {
            tech.kayys.wayang.control.dto.AgentTool dto = new tech.kayys.wayang.control.dto.AgentTool();
            dto.toolId = tool.toolId();
            dto.name = tool.name();
            dto.description = tool.description();
            dto.type = mapToolType(tool.type());
            dto.configuration = tool.config() == null ? null : tool.config().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            java.util.Map.Entry::getKey,
                            e -> String.valueOf(e.getValue())));
            return dto;
        }).toList();
    }

    private List<AgentCapability> mapCapabilities(List<tech.kayys.wayang.agent.AgentCapability> capabilities) {
        if (capabilities == null) {
            return null;
        }

        return capabilities.stream().map(capability -> {
            AgentCapability dto = new AgentCapability();
            dto.name = capability.name();
            dto.type = mapCapabilityType(capability.type());
            dto.enabled = true;
            dto.configuration = capability.config() == null ? null : capability.config().toString();
            return dto;
        }).toList();
    }

    private ToolType mapToolType(String type) {
        if (type == null) {
            return ToolType.API_REST;
        }

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "API", "API_CALL", "REST" -> ToolType.API_REST;
            case "GRPC", "API_GRPC" -> ToolType.API_GRPC;
            case "DB", "DATABASE", "DATABASE_QUERY" -> ToolType.DATABASE;
            case "FILE", "FILE_OPERATION", "FILE_SYSTEM" -> ToolType.FILE_SYSTEM;
            case "CODE", "CODE_EXECUTION", "PYTHON", "SCRIPT" -> ToolType.PYTHON_SCRIPT;
            case "SEARCH", "WEB_SEARCH", "WEB_BROWSER" -> ToolType.WEB_BROWSER;
            default -> ToolType.API_REST;
        };
    }

    private CapabilityType mapCapabilityType(AgentCapabilityType type) {
        if (type == null) {
            return CapabilityType.TOOL_USE;
        }

        return switch (type) {
            case PLANNING -> CapabilityType.PLANNING;
            case REASONING -> CapabilityType.REASONING;
            case TOOL_USE -> CapabilityType.TOOL_USE;
            case MEMORY -> CapabilityType.MEMORY;
            case LEARNING -> CapabilityType.WEB_SEARCH;
            case CODE_GENERATION, CODE_ANALYSIS -> CapabilityType.CODE_EXECUTION;
            default -> CapabilityType.TOOL_USE;
        };
    }
}
