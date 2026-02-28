package tech.kayys.wayang.eip.strategy;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.eip.dto.SplitterDto;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DelimiterSplitStrategy implements SplitStrategy {

    @Override
    public Uni<List<Object>> split(Object message, SplitterDto config) {
        return Uni.createFrom().item(() -> {
            if (message instanceof String text && config.expression() != null) {
                String[] parts = text.split(Pattern.quote(config.expression()));
                return Arrays.stream(parts)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            return List.of(message);
        });
    }
}
