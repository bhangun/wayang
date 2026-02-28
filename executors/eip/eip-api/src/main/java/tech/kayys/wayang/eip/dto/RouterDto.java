package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RouterDto(
        @JsonProperty("rules") List<RouteRuleDto> rules,
        @JsonProperty("defaultRoute") String defaultRoute,
        @JsonProperty("failOnNoMatch") boolean failOnNoMatch,
        @JsonProperty("strategy") String strategy) {
}
