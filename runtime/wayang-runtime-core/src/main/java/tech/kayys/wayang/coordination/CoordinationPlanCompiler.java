package tech.kayys.wayang.coordination;

/**
 * Compiles an immutable CoordinationPlan into an executable CoordinationWorkflowDefinition.
 */
public interface CoordinationPlanCompiler {

    CoordinationWorkflowDefinition compile(CoordinationPlan plan);
}
