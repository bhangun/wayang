package tech.kayys.wayang.eip.strategy;

import io.smallrye.mutiny.Uni;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class Base64EncodeTransformer implements MessageTransformer {
    @Override
    public Uni<Object> transform(Object message, Map<String, Object> parameters) {
        return Uni.createFrom().item(() -> {
            if (message == null)
                return null;
            byte[] bytes = message.toString().getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(bytes);
        });
    }
}
