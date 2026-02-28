package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthDto(
        @JsonProperty("type") String type,
        @JsonProperty("credential") String credential) {
}
