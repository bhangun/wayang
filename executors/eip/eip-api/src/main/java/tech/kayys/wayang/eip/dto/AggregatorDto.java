package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AggregatorDto(
        @JsonProperty("correlationKey") String correlationKey,
        @JsonProperty("expectedCount") int expectedCount,
        @JsonProperty("timeoutMs") long timeoutMs,
        @JsonProperty("strategy") String strategy,
        @JsonProperty("completeOnTimeout") boolean completeOnTimeout) {
}
