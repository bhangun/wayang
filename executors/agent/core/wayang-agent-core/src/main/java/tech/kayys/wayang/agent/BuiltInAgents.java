package tech.kayys.wayang.agent;

import java.util.Set;

/**
 * Built-in agents for orchestrator
 */
public record BuiltInAgents(
        BuiltInPlanner planner,
        BuiltInExecutor executor,
        BuiltInEvaluator evaluator) {

    public static BuiltInAgents createDefault() {
        return new BuiltInAgents(
                BuiltInPlanner.createDefault(),
                BuiltInExecutor.createDefault(),
                BuiltInEvaluator.createDefault());
    }
}

/**
 * Built-in Planner - Creates execution plans
 */
record BuiltInPlanner(
        tech.kayys.wayang.agent.planner.PlanningStrategy strategy,
        boolean enableAdaptivePlanning,
        int maxReplanAttempts) {

    public static BuiltInPlanner createDefault() {
        return new BuiltInPlanner(
                tech.kayys.wayang.agent.planner.PlanningStrategy.PLAN_AND_EXECUTE,
                true,
                3);
    }
}

/**
 * Built-in Executor - Executes agent coordination
 */
record BuiltInExecutor(
        ExecutionMode mode,
        int maxParallelTasks,
        boolean enableFailover) {

    public static BuiltInExecutor createDefault() {
        return new BuiltInExecutor(
                ExecutionMode.ADAPTIVE,
                5,
                true);
    }
}

/**
 * Built-in Evaluator - Evaluates agent results
 */
record BuiltInEvaluator(
        Set<EvaluationCriteria> criteria,
        double successThreshold,
        boolean enableContinuousEvaluation) {

    public static BuiltInEvaluator createDefault() {
        return new BuiltInEvaluator(
                Set.of(
                        EvaluationCriteria.CORRECTNESS,
                        EvaluationCriteria.COMPLETENESS,
                        EvaluationCriteria.QUALITY),
                0.8,
                true);
    }
}

/**
 * Execution Mode
 */
enum ExecutionMode {
    SEQUENTIAL, // Execute one by one
    PARALLEL, // Execute concurrently
    ADAPTIVE, // Decide based on context
    PIPELINE // Stream processing
}

/**
 * Evaluation Criteria
 */
enum EvaluationCriteria {
    CORRECTNESS, // Is result correct?
    COMPLETENESS, // Is task fully completed?
    QUALITY, // Quality of output
    EFFICIENCY, // Resource utilization
    SAFETY, // Safety compliance
    ALIGNMENT // Goal alignment
}
