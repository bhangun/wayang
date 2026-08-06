import os
import shutil

package_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-tool/src/main/java/tech/kayys/wayang/tool"

# Clear all existing files
for filename in os.listdir(package_dir):
    file_path = os.path.join(package_dir, filename)
    if os.path.isfile(file_path) or os.path.islink(file_path):
        os.unlink(file_path)
    elif os.path.isdir(file_path):
        shutil.rmtree(file_path)

files = {
    "Tool.java": """package tech.kayys.wayang.tool;

import tech.kayys.wayang.extension.Extension;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Tool extends Extension {
    ToolDescriptor descriptor();
    CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context);
}
""",
    "ToolContext.java": """package tech.kayys.wayang.tool;

import java.util.Map;
import java.util.Optional;

public interface ToolContext {
    Map<String, Object> attributes();
    Optional<Object> getAttribute(String key);
}
""",
    "ToolResult.java": """package tech.kayys.wayang.tool;

import java.util.Map;
import tech.kayys.wayang.extension.Extension;

public interface ToolResult extends Extension {
    Map<String, Object> getOutputs();
    boolean isSuccess();
    String getErrorMessage();
}
""",
    "ToolRegistry.java": """package tech.kayys.wayang.tool;

import tech.kayys.wayang.registry.Registry;
import java.util.Optional;
import java.util.List;

public interface ToolRegistry extends Registry<Tool> {
    Optional<Tool> findByName(String name);
    List<Tool> listTools();
}
""",
    "ToolDescriptor.java": """package tech.kayys.wayang.tool;

import tech.kayys.wayang.descriptor.Descriptor;

public interface ToolDescriptor extends Descriptor {
    String name();
    String description();
    String version();
}
""",
    "ToolInvocation.java": """package tech.kayys.wayang.tool;

import java.util.Map;
import tech.kayys.wayang.extension.Extension;

public interface ToolInvocation extends Extension {
    String name();
    Map<String, Object> arguments();
}
""",
    "ToolExecutor.java": """package tech.kayys.wayang.tool;

import tech.kayys.wayang.extension.Extension;
import java.util.concurrent.CompletableFuture;

public interface ToolExecutor extends Extension {
    CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context);
}
"""
}

for name, content in files.items():
    with open(os.path.join(package_dir, name), "w") as f:
        f.write(content)

print("Created pure Tool SPI interfaces.")
