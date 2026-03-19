package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.ErrorTroubleshootingResult;
import tech.kayys.wayang.assistant.agent.troubleshoot.TroubleshootingService;
import tech.kayys.wayang.tool.spi.Tool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tool SPI implementation that provides troubleshooting guidance for Wayang errors.
 * This tool is discovered via CDI and registered with the tool registry.
 *
 * <p>Injects {@link TroubleshootingService} directly (not WayangAssistantService)
 * to avoid a circular CDI dependency chain.
 */
@ApplicationScoped
public class WayangErrorHelpTool implements Tool {

    @Inject
    public TroubleshootingService troubleshootingService;

    @Override
    public String id() {
        return "wayang-error-help";
    }

    @Override
    public String name() {
        return "Wayang Error Troubleshooter";
    }

    @Override
    public String description() {
        return "Diagnose and provide step-by-step remediation advice for Wayang platform errors. "
                + "Searches documentation and applies known error patterns to give actionable guidance.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "error", Map.of(
                                "type", "string",
                                "description", "The error message or stack trace from Wayang"
                        ),
                        "context", Map.of(
                                "type", "string",
                                "description", "Optional additional context about what you were doing when the error occurred"
                        )
                ),
                "required", List.of("error")
        );
    }

    @Override
    public Uni<Map<String, Object>> execute(Map<String, Object> arguments, Map<String, Object> context) {
        String error = (String) arguments.get("error");
        if (error == null || error.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("'error' parameter is required"));
        }

        String ctx = (String) arguments.getOrDefault("context", "");
        String fullError = ctx.isBlank() ? error : error + "\n\nContext: " + ctx;

        ErrorTroubleshootingResult result = troubleshootingService.troubleshootError(fullError);

        List<Map<String, Object>> docResults = result.getDocumentationResults().stream()
                .map(r -> Map.<String, Object>of(
                        "title", r.getTitle(),
                        "url", r.getUrl(),
                        "snippet", r.getSnippet()
                ))
                .collect(Collectors.toList());

        return Uni.createFrom().item(Map.of(
                "errorMessage", result.getErrorMessage(),
                "advice", result.getAdvice(),
                "documentationResults", docResults
        ));
    }
}
