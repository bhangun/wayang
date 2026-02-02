package tech.kayys.wayang.integration.core.strategy;

import io.smallrye.mutiny.Uni;
import java.util.Map;

public class LowercaseTransformer implements MessageTransformer {
    @Override
    public Uni<Object> transform(Object message, Map<String, Object> parameters) {
        return Uni.createFrom().item(() -> {
            if (message == null)
                return null;
            return message.toString().toLowerCase();
        });
    }
}
