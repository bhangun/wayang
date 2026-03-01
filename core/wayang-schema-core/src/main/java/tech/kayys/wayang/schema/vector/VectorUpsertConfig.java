package tech.kayys.wayang.schema.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorUpsertConfig {

    @JsonProperty(required = true, defaultValue = "in-memory")
    @JsonPropertyDescription("The type of vector store to use (e.g., in-memory, pinecone, qdrant, milvus, pgvector, chroma).")
    private String storeType;

    @JsonProperty
    @JsonPropertyDescription("An optional collection or namespace name where the vectors should be stored.")
    private String collectionName;
}
