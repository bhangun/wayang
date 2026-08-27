package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.resource.Modality;
import java.util.List;
import java.util.Set;

/**
 * Static catalog providing initial seed specifications for standard frontier, open-weights,
 * multimodal, audio, vision, 3D spatial generation, and local models.
 */
public final class DefaultModelCatalog {

    private DefaultModelCatalog() {}

    public static List<ModelSpec> getSeedModels() {
        return List.of(
                // ==========================================
                // 1. Frontier LLM & Multimodal LLMs
                // ==========================================
                new ModelSpec(
                        "gpt-4o", "openai", "openai",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.STRUCTURED_OUTPUT, ModelCapability.VISION, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT, Modality.IMAGE),
                        Set.of(Modality.TEXT),
                        128_000, 16_384, 2.50, 10.00, 850, 0.95, false
                ),
                new ModelSpec(
                        "gpt-4o-mini", "openai", "openai",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.STRUCTURED_OUTPUT, ModelCapability.VISION),
                        Set.of(Modality.TEXT, Modality.IMAGE),
                        Set.of(Modality.TEXT),
                        128_000, 16_384, 0.15, 0.60, 450, 0.82, false
                ),
                new ModelSpec(
                        "claude-3-5-sonnet-20241022", "claude", "anthropic",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.STRUCTURED_OUTPUT, ModelCapability.VISION, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT, Modality.IMAGE),
                        Set.of(Modality.TEXT),
                        200_000, 8_192, 3.00, 15.00, 950, 0.96, false
                ),
                new ModelSpec(
                        "gemini-2.0-flash", "gemini", "google",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.REASONING, ModelCapability.VISION, ModelCapability.AUDIO_UNDERSTANDING),
                        Set.of(Modality.TEXT, Modality.IMAGE, Modality.AUDIO),
                        Set.of(Modality.TEXT),
                        1_000_000, 8_192, 0.10, 0.40, 300, 0.89, false
                ),
                new ModelSpec(
                        "gemini-1.5-pro", "gemini", "google",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.VISION, ModelCapability.AUDIO_UNDERSTANDING, ModelCapability.DOCUMENT_UNDERSTANDING),
                        Set.of(Modality.TEXT, Modality.IMAGE, Modality.AUDIO, Modality.PDF),
                        Set.of(Modality.TEXT),
                        2_000_000, 8_192, 1.25, 5.00, 900, 0.94, false
                ),

                // ==========================================
                // 2. Reasoning Models
                // ==========================================
                new ModelSpec(
                        "o1", "openai", "openai",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.REASONING, ModelCapability.TOOL_CALLING, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT, Modality.IMAGE),
                        Set.of(Modality.TEXT),
                        200_000, 100_000, 15.00, 60.00, 3200, 0.99, false
                ),
                new ModelSpec(
                        "o3-mini", "openai", "openai",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.REASONING, ModelCapability.TOOL_CALLING, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.TEXT),
                        200_000, 100_000, 1.10, 4.40, 1500, 0.92, false
                ),
                new ModelSpec(
                        "deepseek-r1", "deepseek", "deepseek",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.REASONING, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.TEXT),
                        64_000, 8_192, 0.55, 2.19, 2100, 0.95, false
                ),

                // ==========================================
                // 3. Vision & OCR Specialized Models
                // ==========================================
                new ModelSpec(
                        "nougat-ocr-local", "nougat", "gollek",
                        Set.of(ModelCapability.OCR, ModelCapability.DOCUMENT_UNDERSTANDING),
                        Set.of(Modality.IMAGE, Modality.PDF),
                        Set.of(Modality.TEXT),
                        8_192, 4_096, 0.0, 0.0, 350, 0.88, true
                ),
                new ModelSpec(
                        "florence-2-vision-local", "florence", "gollek",
                        Set.of(ModelCapability.VISION, ModelCapability.OCR, ModelCapability.IMAGE_UNDERSTANDING),
                        Set.of(Modality.IMAGE),
                        Set.of(Modality.TEXT),
                        4_096, 1_024, 0.0, 0.0, 180, 0.85, true
                ),

                // ==========================================
                // 4. Speech & Audio Models
                // ==========================================
                new ModelSpec(
                        "whisper-large-v3", "whisper", "groq",
                        Set.of(ModelCapability.SPEECH_TO_TEXT, ModelCapability.AUDIO_UNDERSTANDING),
                        Set.of(Modality.AUDIO),
                        Set.of(Modality.TEXT),
                        4_096, 4_096, 0.10, 0.10, 250, 0.94, false
                ),
                new ModelSpec(
                        "whisper-local", "whisper", "gollek",
                        Set.of(ModelCapability.SPEECH_TO_TEXT, ModelCapability.AUDIO_UNDERSTANDING),
                        Set.of(Modality.AUDIO),
                        Set.of(Modality.TEXT),
                        4_096, 4_096, 0.0, 0.0, 200, 0.90, true
                ),
                new ModelSpec(
                        "elevenlabs-tts", "elevenlabs", "elevenlabs",
                        Set.of(ModelCapability.TEXT_TO_SPEECH),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.AUDIO),
                        4_096, 4_096, 0.30, 0.30, 400, 0.95, false
                ),

                // ==========================================
                // 5. Image & Video Generation Models
                // ==========================================
                new ModelSpec(
                        "flux-1-schnell-local", "flux", "gollek",
                        Set.of(ModelCapability.IMAGE_GENERATION, ModelCapability.IMAGE_EDITING),
                        Set.of(Modality.TEXT, Modality.IMAGE),
                        Set.of(Modality.IMAGE),
                        8_192, 1_024, 0.0, 0.0, 1200, 0.91, true
                ),
                new ModelSpec(
                        "dall-e-3", "openai", "openai",
                        Set.of(ModelCapability.IMAGE_GENERATION),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.IMAGE),
                        8_192, 1_024, 0.04, 0.04, 4500, 0.92, false
                ),
                new ModelSpec(
                        "stable-video-diffusion", "stability", "gollek",
                        Set.of(ModelCapability.VIDEO_GENERATION),
                        Set.of(Modality.IMAGE, Modality.TEXT),
                        Set.of(Modality.VIDEO),
                        8_192, 1_024, 0.0, 0.0, 6000, 0.86, true
                ),

                // ==========================================
                // 6. 3D Spatial & Mesh Generation Models
                // ==========================================
                new ModelSpec(
                        "trellis-3d-local", "trellis", "gollek",
                        Set.of(ModelCapability.THREE_D_GENERATION, ModelCapability.MESH_GENERATION, ModelCapability.THREE_D_UNDERSTANDING),
                        Set.of(Modality.IMAGE, Modality.TEXT),
                        Set.of(Modality.THREE_D),
                        8_192, 2_048, 0.0, 0.0, 2500, 0.90, true
                ),
                new ModelSpec(
                        "shap-e-local", "shap-e", "gollek",
                        Set.of(ModelCapability.THREE_D_GENERATION, ModelCapability.POINT_CLOUD_GENERATION),
                        Set.of(Modality.TEXT, Modality.IMAGE),
                        Set.of(Modality.THREE_D),
                        8_192, 1_024, 0.0, 0.0, 1800, 0.83, true
                ),
                new ModelSpec(
                        "tripo3d-cloud", "tripo", "tripo",
                        Set.of(ModelCapability.THREE_D_GENERATION, ModelCapability.MESH_GENERATION),
                        Set.of(Modality.IMAGE, Modality.TEXT),
                        Set.of(Modality.THREE_D),
                        8_192, 2_048, 0.20, 0.50, 3000, 0.93, false
                ),

                // ==========================================
                // 7. Embeddings & Local Fast Models
                // ==========================================
                new ModelSpec(
                        "text-embedding-3-small", "openai", "openai",
                        Set.of(ModelCapability.EMBEDDING),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.EMBEDDING),
                        8_192, 1_536, 0.02, 0.0, 150, 0.88, false
                ),
                new ModelSpec(
                        "bge-m3-local", "bge", "gollek",
                        Set.of(ModelCapability.EMBEDDING),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.EMBEDDING),
                        8_192, 1_024, 0.0, 0.0, 80, 0.89, true
                ),
                new ModelSpec(
                        "qwen-2.5-coder-32b", "qwen", "openrouter",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.TEXT),
                        32_768, 8_192, 0.20, 0.20, 700, 0.88, false
                ),
                new ModelSpec(
                        "gollek-local", "gollek", "gollek",
                        Set.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION, ModelCapability.TOOL_CALLING, ModelCapability.CODE_GENERATION),
                        Set.of(Modality.TEXT),
                        Set.of(Modality.TEXT),
                        32_768, 4_096, 0.0, 0.0, 150, 0.80, true
                )
        );
    }
}
