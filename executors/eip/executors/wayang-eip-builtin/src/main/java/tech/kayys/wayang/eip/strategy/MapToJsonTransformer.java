package tech.kayys.wayang.eip.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import java.util.Map;

public class MapToJsonTransformer implements MessageTransformer {

    private final ObjectMapper objectMapper;

    public MapToJsonTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Uni<Object> transform(Object message, Map<String, Object> parameters) {
        return Uni.createFrom().item(() -> {
            try {
                boolean pretty = (Boolean) parameters.getOrDefault("pretty", false);
                if (pretty) {
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
                }
                return objectMapper.writeValueAsString(message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize to JSON", e);
            }
        });
    }
}
