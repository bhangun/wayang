package tech.kayys.wayang.eip.model;

import io.smallrye.mutiny.Uni;

public interface MessageChannel {
    Uni<String> send(Object message);

    Uni<Object> receive();

    Uni<Object> peek();

    Uni<Long> size();
}
