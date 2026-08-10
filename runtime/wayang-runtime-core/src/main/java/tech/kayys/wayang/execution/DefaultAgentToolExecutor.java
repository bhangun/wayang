package tech.kayys.wayang.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.tool.ToolInvocation;

@ApplicationScoped
public class DefaultAgentToolExecutor implements AgentToolExecutor {

    @Override
    public CompletionStage<AgentDecision> execute(ToolInvocation invocation) {
        // Simulate checking approval policies.
        if (invocation.name().equals("filesystem.write")) {
            return CompletableFuture.completedFuture(
                new AgentDecision.WaitForApproval("Requires user consent to write file")
            );
        }
        
        return CompletableFuture.completedFuture(
            new AgentDecision.ExecuteTool(invocation)
        );
    }
}
