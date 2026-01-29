package tech.kayys.wayang.integration.core.strategy;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.integration.core.config.SplitterConfig;
import java.util.List;

public interface SplitStrategy {
    Uni<List<Object>> split(Object message, SplitterConfig config);
}
