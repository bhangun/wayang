package tech.kayys.wayang.tool;

import java.util.Map;
import tech.kayys.wayang.extension.Extension;

public interface ToolInvocation extends Extension {
    String name();
    Map<String, Object> arguments();
}
