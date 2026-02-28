package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record TransformerDto(
                @JsonProperty("transformType") String transformType,
                @JsonProperty("script") String script,
                @JsonProperty("parameters") Map<String, Object> parameters) {
}
