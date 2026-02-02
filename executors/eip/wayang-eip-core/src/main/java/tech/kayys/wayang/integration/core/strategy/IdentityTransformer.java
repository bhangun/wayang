package tech.kayys.wayang.integration.core.strategy;

import io.smallrye.mutiny.Uni;
import java.util.Map;

public class IdentityTransformer implements MessageTransformer {
    @Override
    public Uni<Object> transform(Object message, Map<String, Object> parameters) {
        return Uni.createFrom().item(message);
    }
}
