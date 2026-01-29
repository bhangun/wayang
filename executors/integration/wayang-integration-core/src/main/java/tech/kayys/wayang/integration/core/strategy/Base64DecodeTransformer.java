package tech.kayys.wayang.integration.core.strategy;

import io.smallrye.mutiny.Uni;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class Base64DecodeTransformer implements MessageTransformer {
    @Override
    public Uni<Object> transform(Object message, Map<String, Object> parameters) {
        return Uni.createFrom().item(() -> {
            if (message == null)
                return null;
            byte[] decoded = Base64.getDecoder().decode(message.toString());
            return new String(decoded, StandardCharsets.UTF_8);
        });
    }
}
