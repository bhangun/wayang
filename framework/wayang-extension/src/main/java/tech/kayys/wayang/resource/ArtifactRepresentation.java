package tech.kayys.wayang.resource;

import java.util.List;
import java.util.Map;

/**
 * A specific representation extracted from a raw artifact (e.g., OCR text from an image,
 * a transcript chunk from a video, or an embedding for a page).
 *
 * @param id          Unique ID for this representation.
 * @param type        The type of representation (OCR, CAPTION, EMBEDDING, etc.).
 * @param artifactId  The ID of the parent Artifact.
 * @param uri         URI to this specific representation if it is stored separately.
 * @param text        Extracted text content.
 * @param embedding   Vector embedding of this representation.
 * @param timeRange   Temporal range for video/audio segments (e.g. "00:10-00:15").
 * @param pageRange   Page range for documents.
 * @param metadata    Additional metadata specific to this representation.
 */
public record ArtifactRepresentation(
    String id,
    RepresentationType type,
    String artifactId,
    String uri,
    String text,
    List<Double> embedding,
    String timeRange,
    String pageRange,
    Map<String, Object> metadata
) {}
