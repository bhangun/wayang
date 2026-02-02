package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.code.CodeCapability;
import tech.kayys.wayang.agent.type.AgentType;
import tech.kayys.wayang.agent.type.CoderAgent;

import java.util.*;

/**
 * Executor for CoderAgent - handles code generation, analysis, and refactoring
 */
@Executor(executorType = "coder-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 8, supportedNodeTypes = {
        "agent-task", "coder-agent-task", "code-task" })
@ApplicationScoped
public class CoderAgentExecutor extends AbstractAgentExecutor {

    @Override
    public String getExecutorType() {
        return "coder-agent";
    }

    @Override
    protected AgentType getAgentType() {
        return new CoderAgent(
                Set.of("Java", "Python", "JavaScript"),
                Set.of(CodeCapability.CODE_GENERATION, CodeCapability.CODE_REVIEW),
                "JVM");
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("CoderAgentExecutor executing code task: {}", task.nodeId());

        Map<String, Object> context = task.context();

        // Extract code task configuration
        String capabilityName = (String) context.getOrDefault("capability", "CODE_GENERATION");
        CodeCapability capability = CodeCapability.valueOf(capabilityName);
        String language = (String) context.getOrDefault("language", "Java");
        Map<String, Object> codeContext = (Map<String, Object>) context.getOrDefault("codeContext", Map.of());

        // Execute code operation based on capability
        return executeCodeOperation(capability, language, codeContext, task)
                .map(result -> createSuccessResult(task, result))
                .onFailure().recoverWithItem(error -> createFailureResult(task, error));
    }

    /**
     * Execute code operation based on capability
     */
    private Uni<Map<String, Object>> executeCodeOperation(
            CodeCapability capability,
            String language,
            Map<String, Object> codeContext,
            NodeExecutionTask task) {

        return switch (capability) {
            case CODE_GENERATION -> executeCodeGeneration(language, codeContext);
            case CODE_REVIEW -> executeCodeReview(language, codeContext);
            case CODE_REFACTORING -> executeCodeRefactoring(language, codeContext);
            case BUG_FIXING -> executeBugFixing(language, codeContext);
            case TEST_GENERATION -> executeTestGeneration(language, codeContext);
            case DOCUMENTATION -> executeDocumentation(language, codeContext);
            case CODE_EXPLANATION -> executeCodeExplanation(language, codeContext);
            case PERFORMANCE_OPTIMIZATION -> executePerformanceOptimization(language, codeContext);
        };
    }

    private Uni<Map<String, Object>> executeCodeGeneration(String language, Map<String, Object> context) {
        logger.debug("Generating code in {}", language);

        String specification = (String) context.getOrDefault("specification", "");
        String framework = (String) context.getOrDefault("framework", "");

        return Uni.createFrom().item(Map.of(
                "capability", "CODE_GENERATION",
                "language", language,
                "framework", framework,
                "generatedCode", generateStubCode(language, framework),
                "linesGenerated", 42));
    }

    private Uni<Map<String, Object>> executeCodeReview(String language, Map<String, Object> context) {
        logger.debug("Reviewing code in {}", language);

        String code = (String) context.getOrDefault("code", "");

        List<Map<String, Object>> issues = List.of(
                Map.of("type", "style", "severity", "low", "message", "Consider using more descriptive variable names"),
                Map.of("type", "performance", "severity", "medium", "message", "Loop can be optimized using streams"));

        return Uni.createFrom().item(Map.of(
                "capability", "CODE_REVIEW",
                "language", language,
                "issues", issues,
                "overallScore", 8.5,
                "recommendations", List.of("Add more comments", "Extract method")));
    }

    private Uni<Map<String, Object>> executeCodeRefactoring(String language, Map<String, Object> context) {
        logger.debug("Refactoring code in {}", language);

        return Uni.createFrom().item(Map.of(
                "capability", "CODE_REFACTORING",
                "language", language,
                "refactoredCode", "// Refactored code here",
                "improvements", List.of("Extracted methods", "Reduced complexity", "Improved naming")));
    }

    private Uni<Map<String, Object>> executeBugFixing(String language, Map<String, Object> context) {
        logger.debug("Fixing bugs in {}", language);

        String bugDescription = (String) context.getOrDefault("bugDescription", "");

        return Uni.createFrom().item(Map.of(
                "capability", "BUG_FIXING",
                "language", language,
                "bugDescription", bugDescription,
                "fixedCode", "// Fixed code here",
                "rootCause", "Null pointer exception in method call",
                "testsAdded", true));
    }

    private Uni<Map<String, Object>> executeTestGeneration(String language, Map<String, Object> context) {
        logger.debug("Generating tests in {}", language);

        return Uni.createFrom().item(Map.of(
                "capability", "TEST_GENERATION",
                "language", language,
                "testCode", "// Generated test code here",
                "testFramework", "JUnit 5",
                "coverageExpected", 85));
    }

    private Uni<Map<String, Object>> executeDocumentation(String language, Map<String, Object> context) {
        logger.debug("Generating documentation for {}", language);

        return Uni.createFrom().item(Map.of(
                "capability", "DOCUMENTATION",
                "language", language,
                "documentation", "// Generated documentation here",
                "format", "Javadoc",
                "includesExamples", true));
    }

    private Uni<Map<String, Object>> executeCodeExplanation(String language, Map<String, Object> context) {
        logger.debug("Explaining code in {}", language);

        String code = (String) context.getOrDefault("code", "");

        return Uni.createFrom().item(Map.of(
                "capability", "CODE_EXPLANATION",
                "language", language,
                "explanation", "This code implements a sorting algorithm...",
                "complexity", "O(n log n)",
                "keyPrinciples", List.of("Divide and conquer", "Recursion")));
    }

    private Uni<Map<String, Object>> executePerformanceOptimization(String language, Map<String, Object> context) {
        logger.debug("Optimizing performance for {}", language);

        return Uni.createFrom().item(Map.of(
                "capability", "PERFORMANCE_OPTIMIZATION",
                "language", language,
                "optimizedCode", "// Optimized code here",
                "performanceGain", "35%",
                "optimizations", List.of("Caching", "Lazy loading", "Parallel processing")));
    }

    private String generateStubCode(String language, String framework) {
        return switch (language) {
            case "Java" -> "// Java code stub for " + framework;
            case "Python" -> "# Python code stub for " + framework;
            case "JavaScript" -> "// JavaScript code stub for " + framework;
            default -> "// Code stub";
        };
    }

    @Override
    public int getMaxConcurrentTasks() {
        return 8;
    }

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        String agentType = (String) context.get("agentType");
        return "coder-agent".equals(agentType) || "CODER_AGENT".equals(agentType);
    }
}
