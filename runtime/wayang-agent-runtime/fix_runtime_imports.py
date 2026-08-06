import os

base_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/runtime/wayang-agent-runtime/src/main/java/tech/kayys/wayang/agent/impl"

for file in ["DefaultWayangAgent.java", "DefaultWayangAgentBuilder.java"]:
    path = os.path.join(base_dir, file)
    if os.path.exists(path):
        with open(path, "r") as f:
            content = f.read()
        content = content.replace("tech.kayys.wayang.tools.spi.Tool", "tech.kayys.wayang.tool.Tool")
        content = content.replace("tech.kayys.wayang.tools.spi.ToolContext", "tech.kayys.wayang.tool.ToolContext")
        content = content.replace("tech.kayys.wayang.tools.spi.ToolResult", "tech.kayys.wayang.tool.ToolResult")
        with open(path, "w") as f:
            f.write(content)

print("Agent Runtime fixed.")
