package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageStoreDto(
        @JsonProperty("storeType") String storeType,
        @JsonProperty("retentionDays") int retentionDays,
        @JsonProperty("compressed") boolean compressed) {
}
