package tech.kayys.wayang.rag.model;

/**
 * Rich, multimodal citation provenance.
 *
 * @param artifactId       ID of the originating Artifact.
 * @param representationId ID of the specific ArtifactRepresentation.
 * @param sourceUri        The URI of the source.
 * @param page             The page number (if applicable).
 * @param timeRange        The time range (e.g. video/audio segment).
 * @param region           Bounding box or spatial region.
 * @param extractionMethod The method used to extract this provenance.
 * @param retrievalScore   The score achieved during retrieval.
 */
public record Provenance(
    String artifactId,
    String representationId,
    String sourceUri,
    Integer page,
    String timeRange,
    String region,
    String extractionMethod,
    double retrievalScore
) {}
