package tech.kayys.wayang.eip.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.eip.dto.SplitterDto;

import java.util.ArrayList;
import java.util.List;

public class JsonArraySplitStrategy implements SplitStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Uni<List<Object>> split(Object message, SplitterDto config) {
        return Uni.createFrom().item(() -> {
            try {
                if (message instanceof List<?> list) {
                    return new ArrayList<>(list);
                }

                if (message instanceof String json) {
                    JsonNode node = objectMapper.readTree(json);
                    if (node.isArray()) {
                        List<Object> items = new ArrayList<>();
                        node.forEach(item -> items.add(objectMapper.convertValue(item, Object.class)));
                        return items;
                    }
                }

                return List.of(message);
            } catch (Exception e) {
                return List.of(message);
            }
        });
    }
}
