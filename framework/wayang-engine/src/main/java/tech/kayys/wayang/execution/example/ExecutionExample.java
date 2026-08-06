package tech.kayys.wayang.execution.example;

import tech.kayys.wayang.execution.CheckpointStore;
import tech.kayys.wayang.execution.EdgeCondition;
import tech.kayys.wayang.execution.ExecutionGraph;
import tech.kayys.wayang.execution.ExecutionNode;
import tech.kayys.wayang.execution.impl.DefaultExecutionExecutor;
import tech.kayys.wayang.execution.impl.DefaultExecutionScheduler;
import tech.kayys.wayang.execution.impl.InMemoryCheckpointStore;

public class ExecutionExample {

    public static void main(String[] args) {
        // Create graph builder
        ExecutionGraphBuilder builder = new DefaultExecutionGraphBuilder();

        // Create nodes
        ExecutionNode start = builder.createStartNode();
        ExecutionNode prompt = builder.createPromptNode("Generate code",
                "Write a function to calculate fibonacci");
        ExecutionNode test = builder.createToolNode("test",
                "python_executor", "Run unit tests");
        ExecutionNode analyze = builder.createToolNode("analyze",
                "test_analyzer", "Analyze test results");
        ExecutionNode fix = builder.createPromptNode("fix",
                "Fix the failing tests");
        ExecutionNode approve = builder.createApprovalNode("review",
                "Review the code changes");
        ExecutionNode end = builder.createEndNode();

        // Build the graph
        ExecutionGraph graph = builder
                .edge(start, prompt, EdgeCondition.always())
                .edge(prompt, test, EdgeCondition.onSuccess())
                .edge(test, analyze, EdgeCondition.always())
                .edge(analyze, approve, EdgeCondition.onSuccess())
                .edge(approve, end, EdgeCondition.onSuccess())
                .edge(analyze, fix, EdgeCondition.onFailure())
                .edge(fix, test, EdgeCondition.always()) // Loop back to testing
                .build();

        // Create executor with components
        EventBus eventBus = new DefaultEventBus();
        ResourceManager resourceManager = new DefaultResourceManager();
        CheckpointStore checkpointStore = new InMemoryCheckpointStore();

        ExecutionScheduler scheduler = new DefaultExecutionScheduler(
                new ParallelSchedulingStrategy(),
                resourceManager);

        ExecutionExecutor executor = new DefaultExecutionExecutor(
                scheduler, eventBus, checkpointStore);

        // Execute
        ExecutionResult result = executor.execute(graph);

        // Process results
        System.out.println("Execution status: " + result.getStatus());
        System.out.println("Duration: " + result.getDuration());
        System.out.println("Metrics: " + result.getMetrics());

        if (result.getStatus() == ExecutionStatus.COMPLETED) {
            System.out.println("Final output: " + result.getResult());
        } else {
            System.out.println("Failed: " + result.getErrorMessage());
        }
    }
}