package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.assistant.agent.project.ProjectGeneratorService;
import tech.kayys.wayang.project.api.ProjectDescriptor;
import tech.kayys.wayang.tool.spi.Tool;

import java.util.List;
import java.util.Map;

/**
 * Tool for generating Wayang projects from high-level intent.
 *
 * <p>Injects {@link ProjectGeneratorService} directly (not WayangAssistantService)
 * to avoid a circular CDI dependency chain.
 */
@ApplicationScoped
public class WayangProjectGeneratorTool implements Tool {

    @Inject
    public ProjectGeneratorService projectGenerator;

    @Override
    public String id() {
        return "wayang-project-generator";
    }

    @Override
    public String name() {
        return "Wayang Project Generator";
    }

    @Override
    public String description() {
        return "Generate a simple Wayang project descriptor from a high-level intent.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "intent", Map.of(
                                "type", "string",
                                "description", "High-level description of the project to create. Include desired capabilities like 'RAG', 'web search', 'multi-agent', 'workflow', etc."
                        ),
                        "name", Map.of(
                                "type", "string",
                                "description", "Optional project name (auto-generated from intent if not provided)"
                        ),
                        "description", Map.of(
                                "type", "string",
                                "description", "Optional project description"
                        )
                ),
                "required", List.of("intent")
        );
    }

    @Override
    public Uni<Map<String, Object>> execute(Map<String, Object> arguments, Map<String, Object> context) {
        String intent = (String) arguments.get("intent");

        if (intent == null || intent.trim().isEmpty()) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("Intent parameter is required")
            );
        }

        ProjectDescriptor descriptor = projectGenerator.generateProject(intent);
        return Uni.createFrom().item(Map.of(
                "success", true,
                "project", descriptor
        ));
    }
}
