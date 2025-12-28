package tech.kayys.wayang.plugin.dto;

public enum PluginType {
    CORE("core", "Core platform plugins"),
    AGENT("agent", "AI/Agent plugins"),
    CONNECTOR("connector", "Integration connectors"),
    TRANSFORMER("transformer", "Data transformers"),
    KNOWLEDGE("knowledge", "RAG/Vector knowledge plugins"),
    HITL("hitl", "Human-in-the-loop plugins"),
    EXTENSION("extension", "Third-party extensions"),
    PATTERN("pattern", "Workflow patterns"),
    TEMPLATE("template", "Pre-built workflows");

    PluginType(String id, String description) {
        this.id = id;
        this.description = description;
    }

    private final String id;
    private final String description;

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return id;
    }
}
