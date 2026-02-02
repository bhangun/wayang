package tech.kayys.wayang.integration.core.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import java.util.Map;

public class JsonToMapTransformer implements MessageTransformer {

    private final ObjectMapper objectMapper;

    public JsonToMapTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Uni<Object> transform(Object message, Map<String, Object> parameters) {
        return Uni.createFrom().item(() -> {
            try {
                if (message instanceof String json) {
                    return objectMapper.readValue(json, Map.class);
                }
                return message;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse JSON", e);
            }
        });
    }
}
