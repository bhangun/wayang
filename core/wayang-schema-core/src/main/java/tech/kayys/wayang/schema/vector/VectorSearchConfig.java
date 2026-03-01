package tech.kayys.wayang.schema.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchConfig {

    @JsonProperty(required = true, defaultValue = "in-memory")
    @JsonPropertyDescription("The type of vector store to use (e.g., in-memory, pinecone, qdrant, milvus, pgvector, chroma).")
    private String storeType;

    @JsonProperty(required = true, defaultValue = "10")
    @JsonPropertyDescription("The maximum number of results to return.")
    private Integer topK;

    @JsonProperty(defaultValue = "0.0")
    @JsonPropertyDescription("The minimum similarity score threshold.")
    private Double minScore;

    @JsonProperty
    @JsonPropertyDescription("Optional metadata filters to apply to the search.")
    private Map<String, Object> filters;
}
