package tech.kayys.wayang.tool;

import tech.kayys.wayang.registry.Registry;
import java.util.Optional;
import java.util.List;

public interface ToolRegistry extends Registry<Tool> {
    Optional<Tool> findByName(String name);
    List<Tool> listTools();
}
