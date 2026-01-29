package tech.kayys.wayang.integration.core.model;

import io.smallrye.mutiny.Uni;
import java.time.Duration;

public interface MessageStore {
    Uni<String> store(Object message, Duration retention);

    Uni<Object> retrieve(String messageId);

    Uni<Boolean> delete(String messageId);
}
