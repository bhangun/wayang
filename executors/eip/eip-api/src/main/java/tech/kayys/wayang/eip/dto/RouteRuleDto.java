package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RouteRuleDto(
        @JsonProperty("condition") String condition,
        @JsonProperty("targetNode") String targetNode,
        @JsonProperty("priority") int priority) {
}
