package tech.kayys.wayang.provider.routing;

/**
 * First-class capability classification for AI models across text, reasoning, multimodal,
 * audio, vision, image/video generation, 3D/spatial synthesis, and embeddings.
 */
public enum ModelCapability {
    // Text & Reasoning
    CHAT,
    TEXT_GENERATION,
    REASONING,
    TOOL_CALLING,
    STRUCTURED_OUTPUT,
    CODE_GENERATION,

    // Vision & Documents
    VISION,
    OCR,
    IMAGE_UNDERSTANDING,
    DOCUMENT_UNDERSTANDING,

    // Audio & Speech
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH,
    AUDIO_UNDERSTANDING,
    MUSIC_GENERATION,

    // Visual Generation & Editing
    IMAGE_GENERATION,
    IMAGE_EDITING,
    VIDEO_GENERATION,
    VIDEO_UNDERSTANDING,

    // 3D & Spatial Synthesis
    THREE_D_GENERATION,
    THREE_D_UNDERSTANDING,
    MESH_GENERATION,
    POINT_CLOUD_GENERATION,

    // Search & Retrieval
    EMBEDDING,
    RERANKING
}
