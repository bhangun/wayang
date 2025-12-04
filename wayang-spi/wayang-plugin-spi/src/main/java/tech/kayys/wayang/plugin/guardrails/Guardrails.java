package tech.kayys.wayang.plugin.guardrails;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.plugin.ExecutionResult;
import tech.kayys.wayang.plugin.node.NodeContext;
import tech.kayys.wayang.plugin.node.NodeDescriptor;

public interface Guardrails {
    Uni<GuardrailResult> preCheck(NodeContext context, NodeDescriptor descriptor);
    Uni<GuardrailResult> postCheck(ExecutionResult result, NodeDescriptor descriptor);
}