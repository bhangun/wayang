import os

base_dir = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/runtime/wayang-agent-runtime/src/main/java/tech/kayys/wayang/agent/impl"

# Fix DefaultWayangAgent
agent_file = os.path.join(base_dir, "DefaultWayangAgent.java")
with open(agent_file, "r") as f:
    content = f.read()

content = content.replace("package tech.kayys.wayang.agent;", "package tech.kayys.wayang.agent.impl;")
content = content.replace("import tech.kayys.wayang.json.JsonValue;", "import tech.kayys.wayang.json.JsonValue;\nimport tech.kayys.wayang.agent.Agent;\nimport tech.kayys.wayang.agent.WayangAgentListener;\nimport tech.kayys.wayang.agent.PermissionDecision;")
content = content.replace("public final class WayangAgent {", "public final class DefaultWayangAgent implements Agent {")
content = content.replace("public WayangAgent(", "public DefaultWayangAgent(")
content = content.replace("WayangAgent.send", "DefaultWayangAgent.send")

with open(agent_file, "w") as f:
    f.write(content)

# Fix DefaultWayangAgentBuilder
builder_file = os.path.join(base_dir, "DefaultWayangAgentBuilder.java")
with open(builder_file, "r") as f:
    content = f.read()

content = content.replace("package tech.kayys.wayang.agent;", "package tech.kayys.wayang.agent.impl;\n\nimport tech.kayys.wayang.agent.Agent;")
content = content.replace("public class WayangAgentBuilder", "public class DefaultWayangAgentBuilder")
content = content.replace("public WayangAgent build()", "public Agent build()")
content = content.replace("return new WayangAgent(", "return new DefaultWayangAgent(")
content = content.replace("WayangAgentBuilder", "DefaultWayangAgentBuilder")

with open(builder_file, "w") as f:
    f.write(content)

# Fix WayangSessionPersistence
persistence_file = os.path.join(base_dir, "WayangSessionPersistence.java")
with open(persistence_file, "r") as f:
    content = f.read()

content = content.replace("package tech.kayys.wayang.agent;", "package tech.kayys.wayang.agent.impl;")
with open(persistence_file, "w") as f:
    f.write(content)

print("Agent implementations updated.")
