import os

package_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-harness/src/main/java/tech/kayys/wayang/harness/spi"
runtime_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-harness/src/main/java/tech/kayys/wayang/harness/runtime"

interfaces = [
    "HarnessResult", "HarnessConfig", "MemoryService", "ToolRegistry", 
    "ModelRouter", "PluginManager", "ContextRegistry", "PromptRegistry", 
    "EventBus", "MetricsCollector", "ToolResolver", "ToolInvoker", 
    "Planner", "Guardrail", "OutputProcessor", "TelemetryCollector", 
    "CacheProvider", "WayangKernel"
]

for name in interfaces:
    file_path = os.path.join(package_dir, f"{name}.java")
    if not os.path.exists(file_path):
        with open(file_path, "w") as f:
            f.write(f"package tech.kayys.wayang.harness.spi;\n\npublic interface {name} {{\n}}\n")

def fix_imports(file_path, is_runtime=False):
    with open(file_path, "r") as f:
        content = f.read()
    
    if "import java.util.List;" not in content:
        content = content.replace("package ", "import java.util.List;\npackage ", 1)
    if "import tech.kayys.wayang.agent.AgentResponse;" not in content:
        content = content.replace("package ", "import tech.kayys.wayang.agent.AgentResponse;\npackage ", 1)
        
    if is_runtime:
        # Runtime classes need to import the SPI interfaces we just created
        imports = "\n".join([f"import tech.kayys.wayang.harness.spi.{name};" for name in interfaces])
        content = content.replace("package tech.kayys.wayang.harness.runtime;\n", f"package tech.kayys.wayang.harness.runtime;\n\n{imports}\n")
        
    with open(file_path, "w") as f:
        f.write(content)

fix_imports(os.path.join(package_dir, "HarnessRuntime.java"))
fix_imports(os.path.join(package_dir, "Harness.java"))
fix_imports(os.path.join(runtime_dir, "DefaultHarnessRuntime.java"), is_runtime=True)
