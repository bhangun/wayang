package tech.kayys.wayang.execution;

/**
 * Built-in node types for the execution engine.
 * 
 * <p>
 * Plugin implementations can define additional node types
 * through the PluginManager.
 */
public enum NodeType {
    // Control flow nodes
    START("start"),
    END("end"),
    CONDITION("condition"),
    LOOP("loop"),
    PARALLEL("parallel"),
    MERGE("merge"),
    DELAY("delay"),
    WAIT("wait"),
    WEBHOOK("webhook"),
    APPROVAL("approval"),
    EVENT("event"),

    // AI nodes
    PROMPT("prompt"),
    INFERENCE("inference"),
    REFLECTION("reflection"),
    AGENT("agent"),
    WORKFLOW("workflow"),

    // Data nodes
    MEMORY("memory"),
    RAG("rag"),
    GRAPH_RAG("graph_rag"),
    VARIABLE("variable"),
    SCRIPT("script"),

    // Tool nodes
    TOOL("tool"),
    HTTP("http"),
    MCP("mcp"),
    DATABASE("database"),
    VECTOR_SEARCH("vector_search"),
    BROWSER("browser"),
    SHELL("shell"),
    PYTHON("python"),
    JAVA("java"),
    PLUGIN("plugin");

    private final String type;

    NodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static NodeType fromString(String type) {
        for (NodeType nodeType : values()) {
            if (nodeType.type.equals(type)) {
                return nodeType;
            }
        }
        throw new IllegalArgumentException("Unknown node type: " + type);
    }
}
