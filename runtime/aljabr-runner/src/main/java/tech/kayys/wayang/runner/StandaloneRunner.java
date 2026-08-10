package tech.kayys.wayang.runner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.execution.AgentExecution;
import tech.kayys.wayang.execution.AgentExecutionService;
import tech.kayys.wayang.execution.RuntimeBehavior;

import jakarta.inject.Inject;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Command(name = "aljabr-runner", mixinStandardHelpOptions = true)
public class StandaloneRunner implements Runnable {

    @Inject
    AgentExecutionService executionService;

    @Parameters(paramLabel = "<prompt>", description = "The prompt to execute")
    String prompt;

    @Option(names = {"-m", "--model"}, description = "Model override", defaultValue = "gemini-flash")
    String model;

    @Option(names = {"-b", "--behavior"}, description = "Runtime behavior (FAST, THOROUGH, BALANCED, DEBUG)", defaultValue = "BALANCED")
    RuntimeBehavior behavior;

    @Option(names = {"--json"}, description = "Output AgentContext as JSON", defaultValue = "true")
    boolean jsonOutput;
    
    @Option(names = {"--no-json"}, description = "Disable JSON output and use plaintext")
    boolean noJson;

    @Override
    public void run() {
        try {
            boolean useJson = jsonOutput && !noJson;
            
            // For CI/CD purposes, we generate a synthetic AgentDefinition
            AgentDefinition def = new AgentDefinition();
            def.setId("standalone-agent");
            def.setModel(model);
            
            String sessionId = UUID.randomUUID().toString();
            
            AgentExecution execution = executionService.create(def, sessionId);
            
            // In a real implementation we would pass the prompt down to the execution context or agent context here.
            
            AgentResponse response = execution.executeSync();
            
            if (response.hasError()) {
                if (useJson) {
                    System.out.println(createMapper().writeValueAsString(response));
                } else {
                    System.err.println("Execution failed: " + response.error());
                }
                System.exit(1);
            }
            
            if (useJson) {
                // The user explicitly requested outputting the final AgentContext state as JSON
                System.out.println(createMapper().writeValueAsString(execution.agentContext()));
            } else {
                System.out.println("Execution Completed Successfully.");
                System.out.println("Result: " + response.content());
            }
            
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
