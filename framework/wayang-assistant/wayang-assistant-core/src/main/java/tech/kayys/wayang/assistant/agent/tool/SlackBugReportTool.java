package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.tool.spi.Tool;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tool for reporting bugs to Slack.
 */
@ApplicationScoped
public class SlackBugReportTool implements Tool {

    private static final Logger LOG = Logger.getLogger(SlackBugReportTool.class);
    private static final String SLACK_TOKEN = System.getenv("SLACK_BOT_TOKEN");
    private static final String DEFAULT_CHANNEL = System.getenv("SLACK_BUG_REPORT_CHANNEL");
    
    // Package-private for testing
    HttpClient httpClient = HttpClient.newHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String id() {
        return "slack-bug-report";
    }

    @Override
    public String name() {
        return "Slack Bug Reporter";
    }

    @Override
    public String description() {
        return "Report a bug or a detected issue to the Wayang development team via Slack. " +
               "Use this tool when the user explicitly mentions a bug, a crash, or an unexpected platform behavior.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "bugDescription", Map.of(
                                "type", "string",
                                "description", "Detailed description of the bug or issue"
                        ),
                        "reproSteps", Map.of(
                                "type", "string",
                                "description", "Steps to reproduce the issue (optional)"
                        ),
                        "environment", Map.of(
                                "type", "string",
                                "description", "Environment details (JDK version, OS, etc.)"
                        )
                ),
                "required", List.of("bugDescription")
        );
    }

    @Override
    public Uni<Map<String, Object>> execute(Map<String, Object> arguments, Map<String, Object> context) {
        String bugDescription = (String) arguments.get("bugDescription");
        String reproSteps = (String) arguments.getOrDefault("reproSteps", "N/A");
        String env = (String) arguments.getOrDefault("environment", "Unknown");

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("*[Wayang Assistant] New Bug Report*\n\n");
        messageBuilder.append("*Description:* ").append(bugDescription).append("\n");
        messageBuilder.append("*Reproduction Steps:* ").append(reproSteps).append("\n");
        messageBuilder.append("*Environment:* ").append(env).append("\n");
        messageBuilder.append("*Reported at:* ").append(new java.util.Date()).append("\n");

        return Uni.createFrom().emitter(emitter -> {
            try {
                Map<String, String> body = Map.of(
                        "channel", DEFAULT_CHANNEL,
                        "text", messageBuilder.toString()
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://slack.com/api/chat.postMessage"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + SLACK_TOKEN)
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                LOG.infof("Bug report sent to Slack: %s", response.body());
                                emitter.complete(Map.of(
                                        "status", "success",
                                        "message", "Bug report has been sent to Slack.",
                                        "slackResponse", response.body()
                                ));
                            } else {
                                LOG.errorf("Failed to send bug report to Slack: %d - %s", response.statusCode(), response.body());
                                emitter.fail(new RuntimeException("Slack API error: " + response.statusCode()));
                            }
                        })
                        .exceptionally(ex -> {
                            LOG.error("Exception sending bug report to Slack", ex);
                            emitter.fail(ex);
                            return null;
                        });

            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }
}
