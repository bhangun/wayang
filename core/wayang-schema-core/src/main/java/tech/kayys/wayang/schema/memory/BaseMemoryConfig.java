package tech.kayys.wayang.schema.memory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMemoryConfig {

    @JsonProperty(required = true, defaultValue = "STORE")
    @JsonPropertyDescription("The memory operation to perform (e.g., STORE, RETRIEVE, SEARCH, UPDATE, DELETE, CLEAR, CONTEXT, CONSOLIDATE, STATS).")
    private String operation;

    @JsonProperty(defaultValue = "10")
    @JsonPropertyDescription("The maximum number of results to return for search or retrieve operations.")
    private Integer limit;

    @JsonProperty(defaultValue = "0.0")
    @JsonPropertyDescription("The minimum similarity threshold for search operations.")
    private Double minSimilarity;

    @JsonProperty
    @JsonPropertyDescription("Optional metadata filters to apply to the search or retrieve operations.")
    private Map<String, Object> filters;
}
