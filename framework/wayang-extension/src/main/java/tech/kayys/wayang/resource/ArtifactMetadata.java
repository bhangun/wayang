package tech.kayys.wayang.resource;

import java.time.Duration;
import java.util.Map;

/**
 * Rich metadata for a multimodal artifact.
 *
 * @param mimeType   The MIME type of the artifact content.
 * @param sizeBytes  Size of the artifact in bytes.
 * @param uri        Original or storage URI.
 * @param duration   Duration for audio/video artifacts.
 * @param width      Width for images/video.
 * @param height     Height for images/video.
 * @param pages      Page count for documents (e.g. PDF).
 * @param language   Language of the content.
 * @param attributes Additional metadata (e.g. EXIF, camera data).
 */
public record ArtifactMetadata(
    String mimeType,
    long sizeBytes,
    String uri,
    Duration duration,
    Integer width,
    Integer height,
    Integer pages,
    String language,
    Map<String, Object> attributes
) {}
