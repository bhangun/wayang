package tech.kayys.wayang.tool.albiruni;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.albiruni.api.*;
import tech.kayys.albiruni.core.PublicationPipeline;
import tech.kayys.wayang.descriptor.CapabilityDescriptor;
import tech.kayys.wayang.descriptor.ParameterDescriptor;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolDescriptor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Wayang Agent Tool enabling autonomous AI agents to generate structured educational publications,
 * infographics, scientific figures, diagrams, and study guides.
 */
@ApplicationScoped
public class AlbiruniPublicationTool implements Tool {

    private final ResourceId.ToolId id = new ResourceId.ToolId(Id.random());
    private final Metadata metadata = Metadata.builder()
            .name("generate_educational_publication")
            .description("Synthesizes publication-grade infographics, posters, and study guides from text, papers, and knowledge sources using Al-Biruni.")
            .version(Version.VERSION_1_0_0)
            .build();

    @Override
    public ResourceId id() {
        return id;
    }

    @Override
    public ResourceType type() {
        return new ResourceType.Tool();
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor() {
            @Override public ResourceId id() { return id; }
            @Override public ResourceType type() { return new ResourceType.Tool(); }
            @Override public Metadata metadata() { return metadata; }
            @Override public Set<String> tags() { return Set.of("publication", "infographic", "education", "design", "albiruni"); }
            @Override public Set<String> categories() { return Set.of("design", "education", "multimodal"); }
            @Override public Map<String, ParameterDescriptor> inputs() { return Map.of(); }
            @Override public Map<String, ParameterDescriptor> outputs() { return Map.of(); }
            @Override public List<CapabilityDescriptor> capabilities() { return List.of(); }
            @Override public String name() { return "generate_educational_publication"; }
            @Override public String description() { return metadata.description(); }
            @Override public String version() { return "1.0.0"; }

            @Override
            public Map<String, Object> inputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                Map<String, Object> props = new HashMap<>();
                props.put("prompt", Map.of("type", "string", "description", "Educational text, topic, or detailed prompt to visualize"));
                props.put("type", Map.of("type", "string", "enum", List.of("INFOGRAPHIC", "POSTER", "DIAGRAM", "ILLUSTRATION", "SCIENTIFIC_FIGURE", "PRESENTATION", "WORKSHEET", "HANDOUT"), "description", "Publication format type"));
                props.put("audience", Map.of("type", "string", "description", "Audience level (e.g. 'primary', 'secondary', 'university', 'research')"));
                props.put("language", Map.of("type", "string", "description", "Target language locale (e.g. 'en', 'id', 'es')"));
                props.put("output_dir", Map.of("type", "string", "description", "Output directory path for SVG, PDF, and PNG artifacts"));
                props.put("theme", Map.of("type", "string", "description", "Theme preset (e.g. 'scientific-modern', 'journal-academic', 'classic-clean')"));
                schema.put("properties", props);
                schema.put("required", List.of("prompt"));
                return schema;
            }
        };
    }

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> args = invocation.arguments();
                String prompt = (String) args.getOrDefault("prompt", "");
                String typeStr = (String) args.getOrDefault("type", "INFOGRAPHIC");
                String audience = (String) args.getOrDefault("audience", "university");
                String language = (String) args.getOrDefault("language", "en");
                String outDirStr = (String) args.getOrDefault("output_dir", "./out");
                String theme = (String) args.getOrDefault("theme", "scientific-modern");

                PublicationType pubType;
                try {
                    pubType = PublicationType.valueOf(typeStr.toUpperCase());
                } catch (Exception e) {
                    pubType = PublicationType.INFOGRAPHIC;
                }

                PublicationRequest request = new PublicationRequest(
                    prompt,
                    pubType,
                    audience,
                    language,
                    List.of(),
                    List.of(),
                    OutputFormat.ALL,
                    theme,
                    Map.of()
                );

                PublicationPipeline pipeline = new PublicationPipeline();
                var pipelineResult = pipeline.generate(request).await().indefinitely();

                Path outDir = Path.of(outDirStr);
                Files.createDirectories(outDir);

                var renderedArtifacts = pipeline.renderer().render(pipelineResult, OutputFormat.ALL, outDir).await().indefinitely();

                Map<String, Object> outputs = new HashMap<>();
                outputs.put("concepts_count", pipelineResult.semanticDocument().concepts().size());
                outputs.put("relationships_count", pipelineResult.semanticDocument().relationships().size());
                outputs.put("regions_count", pipelineResult.visualPlan().regions().size());
                outputs.put("components_count", pipelineResult.visualPlan().components().size());

                Map<String, String> files = new HashMap<>();
                for (var entry : renderedArtifacts.entrySet()) {
                    files.put(entry.getKey().name().toLowerCase(), entry.getValue().path().toAbsolutePath().toString());
                }
                outputs.put("rendered_files", files);

                return success(outputs);

            } catch (Exception e) {
                return failure("Al-Biruni publication generation failed: " + e.getMessage());
            }
        });
    }

    private ToolResult success(Map<String, Object> outputs) {
        return new ToolResult() {
            @Override public ResourceId id() { return id; }
            @Override public ResourceType type() { return new ResourceType.Tool(); }
            @Override public Metadata metadata() { return metadata; }
            @Override public Map<String, Object> getOutputs() { return outputs; }
            @Override public boolean isSuccess() { return true; }
            @Override public String getErrorMessage() { return null; }
        };
    }

    private ToolResult failure(String errorMessage) {
        return new ToolResult() {
            @Override public ResourceId id() { return id; }
            @Override public ResourceType type() { return new ResourceType.Tool(); }
            @Override public Metadata metadata() { return metadata; }
            @Override public Map<String, Object> getOutputs() { return Map.of(); }
            @Override public boolean isSuccess() { return false; }
            @Override public String getErrorMessage() { return errorMessage; }
        };
    }
}
