package tech.kayys.wayang.tool;

import java.util.Map;
import tech.kayys.wayang.extension.Extension;

public interface ToolResult extends Extension {
    Map<String, Object> getOutputs();
    boolean isSuccess();
    String getErrorMessage();
}
