package tech.kayys.wayang.context.api.model;

import java.nio.file.Path;

public record TierEntry(
        Path path,
        Tier tier,
        String content,
        long tokens,
        Integer hopDistance
) {
    public TierEntry {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (tier == null) throw new IllegalArgumentException("tier must not be null");
        if (content == null) content = "";
    }
}
