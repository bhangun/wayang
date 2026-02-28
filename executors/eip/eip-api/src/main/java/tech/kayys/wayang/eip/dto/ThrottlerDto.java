package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThrottlerDto(
        @JsonProperty("rate") int rate,
        @JsonProperty("nodeType") String nodeType) {
}
