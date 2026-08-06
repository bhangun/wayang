import os

package_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-harness/src/main/java/tech/kayys/wayang/harness/spi"
runtime_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-harness/src/main/java/tech/kayys/wayang/harness/runtime"

os.makedirs(package_dir, exist_ok=True)
os.makedirs(runtime_dir, exist_ok=True)

files = {
    "Harness.java": """package tech.kayys.wayang.harness.spi;
public interface Harness {
    HarnessResult execute(HarnessRequest request);
}
""",
    "HarnessPhase.java": """package tech.kayys.wayang.harness.spi;
public interface HarnessPhase<I, O> {
    O execute(I input, HarnessContext context, HarnessRuntime runtime);
}
""",
    "HarnessRequest.java": """package tech.kayys.wayang.harness.spi;
import tech.kayys.wayang.agent.AgentRequest;
public interface HarnessRequest {
    AgentRequest getAgentRequest();
    HarnessConfig getConfig();
}
""",
    "HarnessResult.java": """package tech.kayys.wayang.harness.spi;
import tech.kayys.wayang.agent.AgentResponse;
public interface HarnessResult {
    AgentResponse getAgentResponse();
}
""",
    "HarnessContext.java": """package tech.kayys.wayang.harness.spi;
import tech.kayys.wayang.agent.AgentContext;
public interface HarnessContext {
    AgentContext getAgentContext();
}
""",
    "HarnessConfig.java": """package tech.kayys.wayang.harness.spi;
public interface HarnessConfig {
}
""",
    "HarnessRuntime.java": """package tech.kayys.wayang.harness.spi;
public interface HarnessRuntime {
    // Shared services will be defined here (MemoryService, ToolRegistry, etc.)
}
""",
    "PromptBundle.java": """package tech.kayys.wayang.harness.spi;
import java.util.List;
public interface PromptBundle {
    List<String> getSystemPrompts();
    List<String> getUserPrompts();
}
""",
    "ExecutionPlan.java": """package tech.kayys.wayang.harness.spi;
import java.util.List;
public interface ExecutionPlan {
    List<String> getSteps();
}
"""
}

for name, content in files.items():
    with open(os.path.join(package_dir, name), "w") as f:
        f.write(content)

print("Created SPI interfaces.")
