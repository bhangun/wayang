package tech.kayys.wayang.spi.capability.event;

import java.time.Instant;
import java.util.Map;

public interface CapabilityEvent {

    String eventId();

    Instant timestamp();

    String capabilityId();

    String providerId();

    Map<String, Object> metadata();
}
