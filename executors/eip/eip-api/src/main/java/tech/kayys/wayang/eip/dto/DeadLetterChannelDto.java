package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeadLetterChannelDto(
        @JsonProperty("channelName") String channelName,
        @JsonProperty("logErrors") boolean logErrors,
        @JsonProperty("notifyAdmin") boolean notifyAdmin,
        @JsonProperty("retentionDays") int retentionDays) {
}
