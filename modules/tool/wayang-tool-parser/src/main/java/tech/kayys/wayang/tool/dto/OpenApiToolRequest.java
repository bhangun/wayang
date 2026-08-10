package tech.kayys.wayang.tool.dto;

public class OpenApiToolRequest {
    private String namespace;
    private String url;
    private String sourceType;
    private String source;
    private String authProfileId;
    private java.util.Map<String, Object> guardrailsConfig;

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getAuthProfileId() { return authProfileId; }
    public void setAuthProfileId(String authProfileId) { this.authProfileId = authProfileId; }
    public java.util.Map<String, Object> getGuardrailsConfig() { return guardrailsConfig; }
    public void setGuardrailsConfig(java.util.Map<String, Object> guardrailsConfig) { this.guardrailsConfig = guardrailsConfig; }
}
