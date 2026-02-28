package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilterDto(
        @JsonProperty("expression") String expression,
        @JsonProperty("inverse") boolean inverse,
        @JsonProperty("onFilteredRoute") String onFilteredRoute) {
}
