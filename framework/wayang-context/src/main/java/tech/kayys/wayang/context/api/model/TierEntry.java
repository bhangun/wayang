package tech.kayys.wayang.context.api.model;

import tech.kayys.wayang.resource.ContentPart;
import java.nio.file.Path;
import java.util.List;

public record TierEntry(
        Path path,
        Tier tier,
        List<ContentPart> contentParts,
        long tokens,
        Integer hopDistance
) {
    public TierEntry {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (tier == null) throw new IllegalArgumentException("tier must not be null");
        if (contentParts == null) contentParts = List.of();
    }
    
    public String content() {
        StringBuilder sb = new StringBuilder();
        for (ContentPart part : contentParts) {
            if (part instanceof ContentPart.Text t) {
                sb.append(t.text());
            }
        }
        return sb.toString();
    }
}
