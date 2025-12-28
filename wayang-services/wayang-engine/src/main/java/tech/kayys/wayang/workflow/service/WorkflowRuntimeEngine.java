package tech.kayys.wayang.workflow.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

import tech.kayys.wayang.common.spi.ExecutionResult;
import tech.kayys.wayang.node.model.ExecutionContext;
import tech.kayys.wayang.schema.workflow.WorkflowDefinition;

@ApplicationScoped
public class WorkflowRuntimeEngine {

    public Uni<ExecutionResult> executeWorkflow(WorkflowDefinition workflow, Map<String, Object> input,
            ExecutionContext context) {
        // Updated to use the correct ExecutionContext package
        return Uni.createFrom().item(new ExecutionResult(null, null, null, null, null, input));
    }

}
