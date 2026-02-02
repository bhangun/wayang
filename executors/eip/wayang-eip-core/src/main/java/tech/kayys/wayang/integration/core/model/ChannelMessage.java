package tech.kayys.wayang.integration.core.model;

import java.time.Instant;

public record ChannelMessage(String id, Object payload, Instant timestamp) {
}
