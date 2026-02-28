package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IdempotentReceiverDto(
        @JsonProperty("idempotencyKeyField") String idempotencyKeyField,
        @JsonProperty("windowHours") int windowHours,
        @JsonProperty("action") String action) {
}
