package tech.kayys.wayang.tool.image;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolDescriptor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Agent tool tailored for publication design, book illustrations, editorial visual creation,
 * and social campaign asset production.
 *
 * <p>Encapsulates prompt enhancement, style presets, and multi-resolution rendering
 * following the Single Responsibility and Open/Closed principles.
 */
@ApplicationScoped
public class PublicationIllustrationTool implements Tool {

    @Inject
    ImageGenerationTool imageGenTool;

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor() {
            @Override
            public String name() {
                return "create_publication_illustration";
            }

            @Override
            public String description() {
                return "Generates publication-ready editorial artwork and book illustrations with preset artistic styles and aspect ratios.";
            }

            @Override
            public String version() {
                return "1.0.0";
            }

            @Override
            public Map<String, Object> inputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                Map<String, Object> props = new HashMap<>();
                props.put("concept", Map.of("type", "string", "description", "The core concept or scene to illustrate"));
                props.put("style", Map.of("type", "string", "enum", List.of("watercolor", "editorial_line_art", "digital_painting", "cinematic_photo", "minimalist_vector", "vintage_woodcut"), "description", "Visual style preset for publication"));
                props.put("aspect_ratio", Map.of("type", "string", "enum", List.of("1:1", "16:9", "4:3", "3:4", "9:16"), "description", "Target publication format aspect ratio"));
                props.put("mood", Map.of("type", "string", "description", "Atmospheric mood (e.g. serene, mysterious, energetic)"));
                props.put("seed", Map.of("type", "integer", "description", "Reproducibility seed"));
                schema.put("properties", props);
                schema.put("required", List.of("concept", "style"));
                return schema;
            }
        };
    }

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        Map<String, Object> args = invocation.arguments();
        String concept = (String) args.getOrDefault("concept", "");
        String style = (String) args.getOrDefault("style", "digital_painting");
        String aspectRatio = (String) args.getOrDefault("aspect_ratio", "16:9");
        String mood = (String) args.getOrDefault("mood", "");
        long seed = parseLong(args.get("seed"), 0L);

        int[] dimensions = resolveDimensions(aspectRatio);
        String enhancedPrompt = buildEditorialPrompt(concept, style, mood);
        String negativePrompt = "blurry, low resolution, bad anatomy, deformed, distorted, text watermark, amateur";

        Map<String, Object> subArgs = new HashMap<>();
        subArgs.put("prompt", enhancedPrompt);
        subArgs.put("negative_prompt", negativePrompt);
        subArgs.put("width", dimensions[0]);
        subArgs.put("height", dimensions[1]);
        subArgs.put("steps", 25);
        subArgs.put("guidance_scale", 3.5f);
        subArgs.put("seed", seed);

        ToolInvocation subInvocation = new ToolInvocation() {
            @Override
            public String name() {
                return "generate_image";
            }

            @Override
            public Map<String, Object> arguments() {
                return subArgs;
            }
        };

        return imageGenTool.execute(subInvocation, context);
    }

    private static String buildEditorialPrompt(String concept, String style, String mood) {
        StringBuilder sb = new StringBuilder();
        sb.append(concept).append(", ");
        switch (style) {
            case "watercolor" -> sb.append("delicate watercolor illustration, fine washes, textured paper, vibrant subtle pigment, artistic editorial");
            case "editorial_line_art" -> sb.append("refined ink line art, clean crosshatching, elegant minimalist editorial illustration, high precision");
            case "digital_painting" -> sb.append("masterpiece digital painting, rich color palette, soft ambient lighting, high detail, cover illustration");
            case "cinematic_photo" -> sb.append("cinematic photography, 35mm lens, natural directional lighting, shallow depth of field, award-winning editorial");
            case "minimalist_vector" -> sb.append("modern flat vector design, bold geometric shapes, sophisticated color harmony, contemporary publication art");
            case "vintage_woodcut" -> sb.append("vintage woodcut engraving, intricate linework, classic printmaking aesthetic, historical editorial");
            default -> sb.append("high-quality illustration");
        }
        if (mood != null && !mood.isBlank()) {
            sb.append(", mood: ").append(mood);
        }
        return sb.toString();
    }

    private static int[] resolveDimensions(String ratio) {
        return switch (ratio) {
            case "16:9" -> new int[]{1024, 576};
            case "9:16" -> new int[]{576, 1024};
            case "4:3" -> new int[]{1024, 768};
            case "3:4" -> new int[]{768, 1024};
            default -> new int[]{1024, 1024}; // 1:1
        };
    }

    private static long parseLong(Object val, long fallback) {
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) {
            try { return Long.parseLong(s); } catch (Exception ignored) {}
        }
        return fallback;
    }
}
