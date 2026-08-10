package tech.kayys.wayang.runtime.core;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.runtime.WayangRuntime;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionEngine;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.core.Id;

/**
 * Default implementation of the WayangRuntime contract.
 * This class orchestrates the execution of agents by delegating to the 
 * underlying ExecutionEngine (which is our MVP engine).
 */
@ApplicationScoped
public class DefaultWayangRuntime implements WayangRuntime {

    private final String id;
    private final Metadata metadata;
    private final ResourceType type = new ResourceType.Custom("runtime");

    @Inject
    ExecutionEngine executionEngine;

    public DefaultWayangRuntime() {
        this.id = Id.random().asString();
        this.metadata = Metadata.builder()
            .name("default-wayang-runtime")
            .description("Default Wayang Runtime Orchestrator")
            .version("1.0.0")
            .label("type", "runtime")
            .now()
            .build();
    }

    @Override
    public CompletableFuture<AgentResponse> executeAsync(AgentDefinition agent, AgentRequest request) {
        // Create an ExecutionContext based on the incoming AgentRequest
        ExecutionContext context = ExecutionContext.builder()
            .id(request.id() != null ? Id.fromString(request.id()) : Id.random())
            // Pass necessary data from request to context...
            .build();

        // Delegate to the execution engine and map the result back to AgentResponse
        return executionEngine.executeAsync(agent, context).thenApply(result -> {
            if (result.isSuccess()) {
                AgentResponse.AgentResponseBuilder builder = AgentResponse.builder()
                    .id(result.executionId())
                    .success(true)
                    .content("Execution completed successfully");
                    
                result.outputs().forEach(builder::artifact);
                return builder.build();
            } else {
                return AgentResponse.failure(result.error());
            }
        });
    }

    @Override
    public boolean supports(AgentDefinition agent) {
        // The default runtime serves as the generic fallback, supporting everything for now.
        return true;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ResourceId resourceId() {
        return new ResourceId.CustomId(Id.fromString(id), type);
    }

    @Override
    public ResourceType type() {
        return type;
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }
}
