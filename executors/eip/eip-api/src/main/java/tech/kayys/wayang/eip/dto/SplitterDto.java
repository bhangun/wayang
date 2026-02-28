package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SplitterDto(
                @JsonProperty("expression") String expression,
                @JsonProperty("strategy") String strategy,
                @JsonProperty("parallel") boolean parallel,
                @JsonProperty("batchSize") int batchSize) {
}
