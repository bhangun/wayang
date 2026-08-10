package tech.kayys.wayang.tool;

import tech.kayys.wayang.descriptor.Descriptor;

public interface ToolDescriptor extends Descriptor {
    String name();
    String description();
    String version();
    java.util.Map<String, Object> inputSchema();
}
