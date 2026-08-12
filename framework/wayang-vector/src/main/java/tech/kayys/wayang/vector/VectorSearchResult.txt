package tech.kayys.wayang.vector;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;

import tech.kayys.wayang.context.Document;

/**
 * Vector Search Result
 */
public record VectorSearchResult(
    String id,
    Document document,
    double score,
    Map<String, Object> metadata
) {
    public static VectorSearchResult of(Document document, double score) {
        return new VectorSearchResult(
            document.id(),
            document,
            score,
            Map.of()
        );
    }
}