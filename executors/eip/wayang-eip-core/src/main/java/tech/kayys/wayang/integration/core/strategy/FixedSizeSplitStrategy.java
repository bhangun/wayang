package tech.kayys.wayang.integration.core.strategy;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.integration.core.config.SplitterConfig;

import java.util.ArrayList;
import java.util.List;

public class FixedSizeSplitStrategy implements SplitStrategy {

    @Override
    public Uni<List<Object>> split(Object message, SplitterConfig config) {
        return Uni.createFrom().item(() -> {
            if (message instanceof List<?> list) {
                List<Object> batches = new ArrayList<>();
                for (int i = 0; i < list.size(); i += config.batchSize()) {
                    int end = Math.min(i + config.batchSize(), list.size());
                    batches.add(new ArrayList<>(list.subList(i, end)));
                }
                return batches;
            }

            if (message instanceof String text) {
                // Split string into fixed-size chunks
                List<Object> chunks = new ArrayList<>();
                int chunkSize = config.batchSize();
                for (int i = 0; i < text.length(); i += chunkSize) {
                    chunks.add(text.substring(i, Math.min(i + chunkSize, text.length())));
                }
                return chunks;
            }

            // Single item if can't split
            return List.of(message);
        });
    }
}
