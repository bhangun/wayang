package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CorrelationIdDto(
        @JsonProperty("strategy") String strategy,
        @JsonProperty("extractFrom") String extractFrom,
        @JsonProperty("headerName") String headerName) {
}
