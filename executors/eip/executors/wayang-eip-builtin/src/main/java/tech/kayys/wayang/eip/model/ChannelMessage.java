package tech.kayys.wayang.eip.model;

import java.time.Instant;

public record ChannelMessage(String id, Object payload, Instant timestamp) {
}
