package tech.kayys.wayang.memory.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.memory.service.EmbeddingService;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SpiEmbeddingServiceAdapter implements tech.kayys.wayang.memory.spi.EmbeddingService {

    @Inject
    EmbeddingService delegate;

    @Override
    public Uni<List<Float>> embed(String text) {
        return delegate.embed(text)
                .map(vector -> {
                    List<Float> result = new ArrayList<>(vector.length);
                    for (float value : vector) {
                        result.add(value);
                    }
                    return result;
                });
    }
}
