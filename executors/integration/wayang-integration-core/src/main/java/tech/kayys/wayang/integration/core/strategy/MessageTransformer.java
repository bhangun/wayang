package tech.kayys.wayang.integration.core.strategy;

import io.smallrye.mutiny.Uni;
import java.util.Map;

public interface MessageTransformer {
    Uni<Object> transform(Object message, Map<String, Object> parameters);
}
