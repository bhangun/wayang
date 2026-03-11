package tech.kayys.wayang.assistant.api;

import jakarta.inject.Inject;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import tech.kayys.wayang.assistant.agent.WayangAssistantService;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.*;
import tech.kayys.wayang.project.api.ProjectDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for the Wayang Internal Assistant.
 * Provides endpoints for asking questions, generating projects, and troubleshooting errors.
 */
@Path("/api/v1/assistant")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WayangAssistantApi {

    private static final Logger LOG = Logger.getLogger(WayangAssistantApi.class);

    @Inject
    WayangAssistantService assistantService;

    /**
     * Ask a question about Wayang capabilities.
     */
    @POST
    @Path("/ask")
    public Response askQuestion(AssistantRequest request) {
        LOG.infof("Processing question: %s", request.getQuestion());

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Question is required"))
                    .build();
        }

        try {
            List<DocSearchResult> docResults = assistantService.searchDocumentation(request.getQuestion());

            Map<String, Object> response = new HashMap<>();
            response.put("question", request.getQuestion());
            response.put("documentationResults", docResults.stream()
                    .map(r -> Map.of(
                            "title", r.getTitle(),
                            "snippet", r.getSnippet(),
                            "url", r.getUrl(),
                            "score", r.getScore()
                    ))
                    .toList());
            response.put("suggestion", generateAnswerSuggestion(request.getQuestion(), docResults));

            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.error("Error processing question", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to process question: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Generate a Wayang project from intent.
     */
    @POST
    @Path("/generate-project")
    public Response generateProject(ProjectGenerationRequest request) {
        LOG.infof("Generating project from intent: %s", request.getIntent());

        if (request.getIntent() == null || request.getIntent().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Intent is required"))
                    .build();
        }

        try {
            ProjectDescriptor descriptor = assistantService.generateProject(request.getIntent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("project", descriptor);
            response.put("summary", generateProjectSummary(descriptor));
            response.put("nextSteps", List.of(
                    "Review the generated project structure",
                    "Customize workflow nodes and configurations",
                    "Add your own prompts and templates",
                    "Configure inference providers and models",
                    "Test the workflow using the execution API"
            ));

            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.error("Error generating project", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to generate project: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Troubleshoot an error message.
     */
    @POST
    @Path("/troubleshoot")
    public Response troubleshootError(ErrorTroubleshootingRequest request) {
        LOG.infof("Troubleshooting error: %s", request.getErrorMessage());

        if (request.getErrorMessage() == null || request.getErrorMessage().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Error message is required"))
                    .build();
        }

        try {
            ErrorTroubleshootingResult result = assistantService.troubleshootError(request.getErrorMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", result.getErrorMessage());
            response.put("advice", result.getAdvice());
            response.put("documentationResults", result.getDocumentationResults().stream()
                    .map(r -> Map.of(
                            "title", r.getTitle(),
                            "snippet", r.getSnippet(),
                            "url", r.getUrl()
                    ))
                    .toList());
            response.put("additionalHelp", List.of(
                    "Check application.properties for configuration issues",
                    "Review logs for additional context",
                    "Ensure all dependencies are properly declared",
                    "Verify that required services (PostgreSQL, Kafka) are running"
            ));

            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.error("Error troubleshooting", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to troubleshoot: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get assistant capabilities.
     */
    @GET
    @Path("/capabilities")
    public Response getCapabilities() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Wayang Internal Assistant");
        response.put("version", "1.0.0");
        response.put("capabilities", List.of(
                Map.of(
                        "id", "ask",
                        "name", "Ask Questions",
                        "description", "Get answers about Wayang platform capabilities",
                        "endpoint", "/api/v1/assistant/ask"
                ),
                Map.of(
                        "id", "generate-project",
                        "name", "Generate Project",
                        "description", "Create Wayang projects from high-level intent",
                        "endpoint", "/api/v1/assistant/generate-project"
                ),
                Map.of(
                        "id", "troubleshoot",
                        "name", "Troubleshoot Errors",
                        "description", "Get help with error messages",
                        "endpoint", "/api/v1/assistant/troubleshoot"
                )
        ));
        response.put("tools", List.of(
                Map.of("id", "wayang-doc-search", "name", "Documentation Search"),
                Map.of("id", "wayang-project-generator", "name", "Project Generator"),
                Map.of("id", "wayang-error-help", "name", "Error Troubleshooter")
        ));

        return Response.ok(response).build();
    }

    private String generateAnswerSuggestion(String question, List<DocSearchResult> results) {
        if (results.isEmpty() || "No results found".equals(results.get(0).getTitle())) {
            return "No documentation matches found. Try different keywords.";
        } else if (results.size() == 1) {
            return "Found 1 relevant documentation result.";
        } else {
            return String.format("Found %d relevant documentation results.", results.size());
        }
    }

    private String generateProjectSummary(ProjectDescriptor descriptor) {
        StringBuilder summary = new StringBuilder();
        summary.append("Generated project: ").append(descriptor.getName()).append("\n");
        summary.append("Capabilities: ").append(String.join(", ", descriptor.getCapabilities())).append("\n");
        summary.append("Workflows: ").append(descriptor.getWorkflows().size());
        return summary.toString();
    }

    public static class AssistantRequest {
        private String question;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

    public static class ProjectGenerationRequest {
        private String intent;
        private String name;
        private String description;

        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ErrorTroubleshootingRequest {
        private String errorMessage;
        private String context;

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
}
