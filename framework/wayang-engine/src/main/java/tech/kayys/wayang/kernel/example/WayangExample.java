package tech.kayys.wayang.kernel.example;

public class WayangExample {

    public static void main(String[] args) {
        // Boot the platform
        WayangKernel kernel = Bootstrap.boot();

        // Access runtimes
        GoalRuntime goals = kernel.runtimes().runtime(GoalRuntime.class);
        AgentRuntime agents = kernel.runtimes().runtime(AgentRuntime.class);
        ToolRuntime tools = kernel.runtimes().runtime(ToolRuntime.class);
        MemoryRuntime memory = kernel.runtimes().runtime(MemoryRuntime.class);

        // Access services
        Clock clock = kernel.services().service(Clock.class);
        EventBus events = kernel.services().service(EventBus.class);
        Configuration config = kernel.services().service(Configuration.class);

        // Access capabilities
        Collection<Capability> models = kernel.capabilities().capabilitiesByType(CapabilityType.CHAT_MODEL);

        // Subscribe to events
        events.subscribe(event -> {
            if (event instanceof GoalStartedEvent) {
                System.out.println("Goal started: " + ((GoalStartedEvent) event).getGoalId());
            }
        });

        // Create and execute a goal
        GoalDefinition definition = GoalDefinition.of(
                "Analyze Document",
                "Analyze a document and extract key insights",
                GoalType.ANALYSIS);

        GoalInstance goal = GoalInstance.create(definition);
        GoalResult result = goals.execute(GoalRequest.of(goal));

        if (result.isSuccess()) {
            System.out.println("Goal completed!");
            System.out.println("Output: " + result.getOutput());
        } else {
            System.err.println("Goal failed: " + result.getError());
        }

        // Get kernel metrics
        KernelMetrics metrics = kernel.metrics();
        System.out.println("Total runtimes: " + metrics.getRuntimeCount());
        System.out.println("Total capabilities: " + metrics.getCapabilityCount());

        // Shutdown
        Bootstrap.shutdown();
    }
}
