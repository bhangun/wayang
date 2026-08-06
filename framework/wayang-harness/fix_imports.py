import os

package_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-harness/src/main/java/tech/kayys/wayang/harness/spi"
runtime_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-harness/src/main/java/tech/kayys/wayang/harness/runtime"

def fix_imports(file_path):
    with open(file_path, "r") as f:
        content = f.read()
    
    # Remove bad imports
    lines = content.split('\n')
    good_lines = []
    
    # We will grab package line, then inject our imports
    package_line = ""
    for line in lines:
        if line.startswith("package"):
            package_line = line
        elif not line.startswith("import java.util.List;") and not line.startswith("import tech.kayys.wayang.agent.AgentResponse;") and not line.startswith("import tech.kayys.wayang.harness.spi."):
            good_lines.append(line)
            
    # Now rebuild
    new_lines = [package_line, "import java.util.List;", "import tech.kayys.wayang.agent.AgentResponse;"]
    
    if "DefaultHarnessRuntime" in file_path:
        interfaces = [
            "HarnessResult", "HarnessConfig", "MemoryService", "ToolRegistry", 
            "ModelRouter", "PluginManager", "ContextRegistry", "PromptRegistry", 
            "EventBus", "MetricsCollector", "ToolResolver", "ToolInvoker", 
            "Planner", "Guardrail", "OutputProcessor", "TelemetryCollector", 
            "CacheProvider", "WayangKernel", "HarnessRuntime", "Logger"
        ]
        for name in interfaces:
            new_lines.append(f"import tech.kayys.wayang.harness.spi.{name};")
    
    new_content = "\n".join(new_lines) + "\n" + "\n".join(good_lines)
    
    with open(file_path, "w") as f:
        f.write(new_content)

fix_imports(os.path.join(package_dir, "HarnessRuntime.java"))
fix_imports(os.path.join(package_dir, "Harness.java"))
fix_imports(os.path.join(runtime_dir, "DefaultHarnessRuntime.java"))
