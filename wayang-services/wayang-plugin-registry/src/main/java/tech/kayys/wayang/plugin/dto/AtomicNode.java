package tech.kayys.wayang.plugin.dto;

import io.smallrye.mutiny.Uni;

public interface AtomicNode extends WorkflowNode {
    Uni<NodeExecutionResult> execute(ExecutionContext context);

    default NodeType getType() {
        return NodeType.ATOMIC;
    }
}