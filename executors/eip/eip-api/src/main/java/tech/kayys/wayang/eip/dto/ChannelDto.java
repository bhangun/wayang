package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChannelDto(
        @JsonProperty("channelName") String channelName,
        @JsonProperty("type") String type,
        @JsonProperty("capacity") int capacity,
        @JsonProperty("persistent") boolean persistent,
        @JsonProperty("ttlSeconds") int ttlSeconds) {
}
