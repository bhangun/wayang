package tech.kayys.wayang;

import tech.kayys.wayang.engine.GamelanEngineConfig;
import tech.kayys.wayang.engine.GamelanWorkflowEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Wayang Engine with Gamelan SDK...");

        GamelanEngineConfig config = GamelanEngineConfig.builder()
                .endpoint("http://localhost:8080")
                .tenantId("wayang-dev")
                .apiKey("dev-api-key")
                .build();

        try (GamelanWorkflowEngine engine = new GamelanWorkflowEngine(config)) {
            System.out.println("Gamelan Workflow Engine initialized.");
            
            engine.listWorkflows().subscribe().with(
                workflows -> {
                    System.out.println("Connected to Gamelan. Found " + workflows.size() + " workflows.");
                    workflows.forEach(w -> System.out.println("- " + w.name()));
                },
                failure -> System.err.println("Failed to connect to Gamelan: " + failure.getMessage())
            );

            // Wait for async operations
            Thread.sleep(5000);
        } catch (Exception e) {
            System.err.println("Error initializing Gamelan engine: " + e.getMessage());
        }
    }
}