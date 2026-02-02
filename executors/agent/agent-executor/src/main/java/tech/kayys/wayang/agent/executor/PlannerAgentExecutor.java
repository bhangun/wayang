package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.planner.PlanningStrategy;
import tech.kayys.wayang.agent.type.AgentType;
import tech.kayys.wayang.agent.type.PlannerAgent;

import java.util.*;

/**
 * Executor for PlannerAgent - handles strategic planning and task decomposition
 */
@Executor(executorType = "planner-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 5, supportedNodeTypes = {
        "agent-task", "planner-agent-task", "planning-task" })
@ApplicationScoped
public class PlannerAgentExecutor extends AbstractAgentExecutor {

    @Override
    public String getExecutorType() {
        return "planner-agent";
    }

    @Override
    protected AgentType getAgentType() {
        return new PlannerAgent(
                PlanningStrategy.PLAN_AND_EXECUTE,
                5,
                true);
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("PlannerAgentExecutor executing planning task: {}", task.nodeId());

        Map<String, Object> context = task.context();

        // Extract planning configuration
        String strategyName = (String) context.getOrDefault("strategy", "PLAN_AND_EXECUTE");
        PlanningStrategy strategy = PlanningStrategy.valueOf(strategyName);
        String goal = (String) context.getOrDefault("goal", "");
        Map<String, Object> parameters = (Map<String, Object>) context.getOrDefault("parameters", Map.of());

        // Execute planning based on strategy
        return executePlanning(strategy, goal, parameters, task)
                .map(plan -> createSuccessResult(task, Map.of(
                        "plan", plan,
                        "strategy", strategy.name(),
                        "goal", goal)))
                .onFailure().recoverWithItem(error -> createFailureResult(task, error));
    }

    /**
     * Execute planning based on the selected strategy
     */
    private Uni<Map<String, Object>> executePlanning(
            PlanningStrategy strategy,
            String goal,
            Map<String, Object> parameters,
            NodeExecutionTask task) {

        return switch (strategy) {
            case HIERARCHICAL -> executeHierarchicalPlanning(goal, parameters);
            case CHAIN_OF_THOUGHT -> executeChainOfThought(goal, parameters);
            case TREE_OF_THOUGHT -> executeTreeOfThought(goal, parameters);
            case REACT -> executeReActPlanning(goal, parameters);
            case PLAN_AND_EXECUTE -> executePlanAndExecute(goal, parameters);
            case ADAPTIVE -> executeAdaptivePlanning(goal, parameters);
        };
    }

    private Uni<Map<String, Object>> executeHierarchicalPlanning(
            String goal,
            Map<String, Object> parameters) {

        logger.debug("Executing hierarchical planning for goal: {}", goal);

        // Top-down decomposition
        List<Map<String, Object>> steps = List.of(
                Map.of("step", 1, "action", "Analyze goal", "type", "analysis"),
                Map.of("step", 2, "action", "Break into sub-goals", "type", "decomposition"),
                Map.of("step", 3, "action", "Create execution plan", "type", "planning"));

        return Uni.createFrom().item(Map.of(
                "strategy", "HIERARCHICAL",
                "steps", steps,
                "totalSteps", steps.size()));
    }

    private Uni<Map<String, Object>> executeChainOfThought(
            String goal,
            Map<String, Object> parameters) {

        logger.debug("Executing chain-of-thought planning for goal: {}", goal);

        // Step-by-step reasoning
        List<String> thoughts = List.of(
                "First, understand the requirements",
                "Then, identify dependencies",
                "Next, sequence the steps",
                "Finally, validate the plan");

        return Uni.createFrom().item(Map.of(
                "strategy", "CHAIN_OF_THOUGHT",
                "thoughts", thoughts,
                "reasoning", "Sequential step-by-step approach"));
    }

    private Uni<Map<String, Object>> executeTreeOfThought(
            String goal,
            Map<String, Object> parameters) {

        logger.debug("Executing tree-of-thought planning for goal: {}", goal);

        // Multiple reasoning paths
        List<Map<String, Object>> branches = List.of(
                Map.of("approach", "A", "steps", List.of("A1", "A2", "A3"), "score", 0.8),
                Map.of("approach", "B", "steps", List.of("B1", "B2"), "score", 0.9),
                Map.of("approach", "C", "steps", List.of("C1", "C2", "C3", "C4"), "score", 0.7));

        return Uni.createFrom().item(Map.of(
                "strategy", "TREE_OF_THOUGHT",
                "branches", branches,
                "bestBranch", "B"));
    }

    private Uni<Map<String, Object>> executeReActPlanning(
            String goal,
            Map<String, Object> parameters) {

        logger.debug("Executing ReAct planning for goal: {}", goal);

        // Reasoning + Acting cycles
        List<Map<String, Object>> cycles = List.of(
                Map.of("thought", "Need to gather data", "action", "fetch_data", "observation", "data retrieved"),
                Map.of("thought", "Data needs processing", "action", "process_data", "observation", "processed"),
                Map.of("thought", "Ready to conclude", "action", "finalize", "observation", "completed"));

        return Uni.createFrom().item(Map.of(
                "strategy", "REACT",
                "cycles", cycles,
                "iterative", true));
    }

    private Uni<Map<String, Object>> executePlanAndExecute(
            String goal,
            Map<String, Object> parameters) {

        logger.debug("Executing plan-and-execute for goal: {}", goal);

        // Complete plan before execution
        Map<String, Object> plan = Map.of(
                "phase1", Map.of("name", "Planning", "duration", "2h", "tasks", List.of("analyze", "design")),
                "phase2", Map.of("name", "Execution", "duration", "8h", "tasks", List.of("implement", "test")),
                "phase3", Map.of("name", "Review", "duration", "1h", "tasks", List.of("verify", "deploy")));

        return Uni.createFrom().item(Map.of(
                "strategy", "PLAN_AND_EXECUTE",
                "plan", plan,
                "totalPhases", 3));
    }

    private Uni<Map<String, Object>> executeAdaptivePlanning(
            String goal,
            Map<String, Object> parameters) {

        logger.debug("Executing adaptive planning for goal: {}", goal);

        // Dynamic replanning capability
        return Uni.createFrom().item(Map.of(
                "strategy", "ADAPTIVE",
                "initialPlan", List.of("step1", "step2", "step3"),
                "replanningEnabled", true,
                "maxReplans", 3,
                "adaptiveTriggers", List.of("failure", "context_change", "new_information")));
    }

    @Override
    public int getMaxConcurrentTasks() {
        return 5; // Planning tasks are more resource-intensive
    }

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        String agentType = (String) context.get("agentType");
        return "planner-agent".equals(agentType) || "PLANNER_AGENT".equals(agentType);
    }
}
