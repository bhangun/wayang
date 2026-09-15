package tech.kayys.wayang.tool;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Standard immutable implementation of {@link ToolContext}.
 */
public record SimpleToolContext(
        Map<String, Object> attributes
) implements ToolContext {

    public SimpleToolContext {
        attributes = (attributes == null) ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
    }

    public static SimpleToolContext empty() {
        return new SimpleToolContext(Collections.emptyMap());
    }

    public static SimpleToolContext of(Map<String, Object> attributes) {
        return new SimpleToolContext(attributes);
    }

    @Override
    public Optional<Object> getAttribute(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(attributes.get(key));
    }
}
