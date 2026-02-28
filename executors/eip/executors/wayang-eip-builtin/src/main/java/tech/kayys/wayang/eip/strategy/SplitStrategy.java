package tech.kayys.wayang.eip.strategy;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.eip.dto.SplitterDto;
import java.util.List;

public interface SplitStrategy {
    Uni<List<Object>> split(Object message, SplitterDto config);
}
