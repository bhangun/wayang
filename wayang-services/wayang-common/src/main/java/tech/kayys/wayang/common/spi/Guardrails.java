package tech.kayys.wayang.common.spi;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.schema.node.NodeDefinition;

public interface Guardrails {
    Uni<GuardrailResult> preCheck(NodeContext context, NodeDefinition descriptor);
    Uni<GuardrailResult> postCheck(ExecutionResult result, NodeDefinition descriptor);
}


