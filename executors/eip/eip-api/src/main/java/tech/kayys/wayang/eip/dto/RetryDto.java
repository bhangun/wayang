package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RetryDto(
        @JsonProperty("maxAttempts") int maxAttempts,
        @JsonProperty("initialDelayMs") long initialDelayMs,
        @JsonProperty("maxDelayMs") long maxDelayMs,
        @JsonProperty("backoffMultiplier") double backoffMultiplier,
        @JsonProperty("retryableExceptions") List<String> retryableExceptions) {
}
