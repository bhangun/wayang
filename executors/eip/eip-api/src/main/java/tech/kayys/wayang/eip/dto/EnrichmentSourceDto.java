package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record EnrichmentSourceDto(
        @JsonProperty("type") String type,
        @JsonProperty("uri") String uri,
        @JsonProperty("mapping") Map<String, String> mapping) {
}
