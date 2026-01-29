package tech.kayys.wayang.integration.core.model;

import io.smallrye.mutiny.Uni;

public interface MessageChannel {
    Uni<String> send(Object message);

    Uni<Object> receive();

    Uni<Object> peek();

    Uni<Long> size();
}
