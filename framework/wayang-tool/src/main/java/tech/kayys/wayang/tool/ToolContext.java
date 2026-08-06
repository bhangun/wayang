package tech.kayys.wayang.tool;

import java.util.Map;
import java.util.Optional;

public interface ToolContext {
    Map<String, Object> attributes();
    Optional<Object> getAttribute(String key);
}
