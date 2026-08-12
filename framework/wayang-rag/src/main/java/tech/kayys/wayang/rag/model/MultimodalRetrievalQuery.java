package tech.kayys.wayang.rag.model;

import java.util.List;
import java.util.Map;

/**
 * A multimodal retrieval query.
 *
 * @param parts    The parts of the query (e.g. text, images).
 * @param mode     The retrieval mode (e.g., hybrid, semantic).
 * @param topK     Number of results to return.
 * @param minScore Minimum score threshold.
 * @param filters  Metadata filters to apply during retrieval.
 */
public record MultimodalRetrievalQuery(
    List<QueryPart> parts,
    String mode,
    int topK,
    double minScore,
    Map<String, Object> filters
) {}
