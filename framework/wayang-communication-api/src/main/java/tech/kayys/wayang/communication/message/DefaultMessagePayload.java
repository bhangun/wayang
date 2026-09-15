package tech.kayys.wayang.communication.message;

import java.util.Objects;

public record DefaultMessagePayload(
        String mediaType,
        Object value
) implements MessagePayload {

    public DefaultMessagePayload {
        Objects.requireNonNull(mediaType, "mediaType");
        Objects.requireNonNull(value, "value");
    }
}
