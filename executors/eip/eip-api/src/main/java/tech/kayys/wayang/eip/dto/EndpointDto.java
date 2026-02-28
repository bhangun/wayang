package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record EndpointDto(
        @JsonProperty("uri") String uri,
        @JsonProperty("protocol") String protocol,
        @JsonProperty("headers") Map<String, String> headers,
        @JsonProperty("properties") Map<String, Object> properties,
        @JsonProperty("auth") AuthDto auth,
        @JsonProperty("timeoutMs") int timeoutMs,
        @JsonProperty("async") boolean async) {
}
