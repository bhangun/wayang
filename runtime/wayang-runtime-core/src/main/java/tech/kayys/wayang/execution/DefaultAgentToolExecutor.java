package tech.kayys.wayang.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.tool.ToolInvocation;

@ApplicationScoped
public class DefaultAgentToolExecutor implements AgentToolExecutor {

    @Override
    public CompletionStage<AgentDecision> execute(ToolInvocation invocation) {
        // In Phase 1, we simulate checking approval policies.
        // If a real approval strategy was wired, it would return WaitForApproval.
        // For now, we simulate execution or bypass directly to execution.
        
        return CompletableFuture.completedFuture(
            new AgentDecision.ExecuteTool(invocation)
        );
    }
}
