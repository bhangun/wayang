package tech.kayys.wayang.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.execution.AgentExecution;
import tech.kayys.wayang.execution.AgentExecutionService;
import tech.kayys.wayang.execution.ExecutionBudget;
import tech.kayys.wayang.execution.RuntimeBehavior;
import tech.kayys.wayang.extension.Metadata;

/**
 * Wayang Standalone Agent Runner — headless CI/CD entry point for Wayang agents.
 *
 * <p>Runs a single agent turn synchronously and exits with code {@code 0} on
 * success or {@code 1} on failure. Output is JSON by default (disable with
 * {@code --no-json}).</p>
 *
 * <p>Usage examples:</p>
 * <pre>
 *   wayang-runner "Summarise the README"
 *   wayang-runner --model gemini-pro --behavior THOROUGH "Analyse code quality"
 *   wayang-runner --no-json "What is 2 + 2"
 * </pre>
 */
@Command(
    name = "wayang-runner",
    mixinStandardHelpOptions = true,
    description = "Standalone CI/CD runner for Wayang agents"
)
public class WayangAgentRunner implements Runnable {

    @Inject
    AgentExecutionService executionService;

    @Parameters(
        paramLabel = "<prompt>",
        description = "The user prompt to execute"
    )
    String prompt;

    @Option(
        names = {"-m", "--model"},
        description = "Model ID override (e.g. gemini-flash, claude-3-5-sonnet)",
        defaultValue = "gemini-flash"
    )
    String model;

    @Option(
        names = {"-b", "--behavior"},
        description = "Runtime behavior: FAST, BALANCED, THOROUGH, DEBUG",
        defaultValue = "BALANCED"
    )
    RuntimeBehavior behavior;

    @Option(
        names = {"--no-json"},
        description = "Disable JSON output and print plain text instead"
    )
    boolean noJson;

    // -------------------------------------------------------------------------

    @Override
    public void run() {
        try {
            // Build a minimal AgentDefinition for headless execution.
            AgentDefinition def = AgentDefinition.builder()
                .metadata(Metadata.builder()
                    .name("wayang-standalone-agent")
                    .description("Headless CI/CD agent")
                    .now()
                    .build())
                .goal("You are a helpful AI assistant. Complete the user's request accurately.")
                .build();

            // Wrap the CLI prompt in an AgentRequest.
            AgentRequest request = AgentRequest.of(prompt);

            // Apply behavior to execution budget.
            ExecutionBudget budget = budgetForBehavior(behavior);

            // Create and run the execution synchronously.
            AgentExecution execution = executionService.create(def, request, budget);
            AgentResponse response = execution.executeSync();

            boolean useJson = !noJson;

            if (!response.success()) {
                handleError(response, useJson);
                System.exit(1);
                return;
            }

            if (useJson) {
                // Output the full response as structured JSON for CI/CD parsing.
                System.out.println(jsonMapper().writeValueAsString(response));
            } else {
                System.out.println(response.content());
            }

            System.exit(0);

        } catch (Exception e) {
            System.err.println("[wayang-runner] Fatal error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void handleError(AgentResponse response, boolean useJson) throws Exception {
        if (useJson) {
            System.err.println(jsonMapper().writeValueAsString(response));
        } else {
            System.err.println("Execution failed: " + response.error());
        }
    }

    private static ExecutionBudget budgetForBehavior(RuntimeBehavior behavior) {
        return switch (behavior) {
            case FAST      -> ExecutionBudget.defaults();   // lower limits
            case THOROUGH  -> ExecutionBudget.defaults();   // expand when budget has explicit profiles
            case DEBUG     -> ExecutionBudget.defaults();
            default        -> ExecutionBudget.defaults();
        };
    }

    private static ObjectMapper jsonMapper() {
        return new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndRegisterModules();
    }
}
