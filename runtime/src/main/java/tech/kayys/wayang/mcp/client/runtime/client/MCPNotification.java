package tech.kayys.wayang.mcp.client.runtime.client;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * Represents an MCP notification message
 * Notifications are one-way messages that don't expect a response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MCPNotification {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private JsonNode params;
    
    // Constructors
    public MCPNotification() {}
    
    public MCPNotification(String method) {
        this.method = method;
    }
    
    public MCPNotification(String method, JsonNode params) {
        this.method = method;
        this.params = params;
    }
    
    // Getters and Setters
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public JsonNode getParams() {
        return params;
    }
    
    public void setParams(JsonNode params) {
        this.params = params;
    }
    
    // Builder pattern methods
    public MCPNotification withMethod(String method) {
        this.method = method;
        return this;
    }
    
    public MCPNotification withParams(JsonNode params) {
        this.params = params;
        return this;
    }
    
    // Utility methods
    public boolean hasParams() {
        return params != null && !params.isNull();
    }
    
    // Standard methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCPNotification that = (MCPNotification) o;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
               Objects.equals(method, that.method) &&
               Objects.equals(params, that.params);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, method, params);
    }
    
    @Override
    public String toString() {
        return "MCPNotification{" +
               "jsonrpc='" + jsonrpc + '\'' +
               ", method='" + method + '\'' +
               ", params=" + params +
               '}';
    }
    
    // Common notification types as constants
    public static class Methods {
        public static final String PROGRESS = "notifications/progress";
        public static final String MESSAGE = "notifications/message";
        public static final String RESOURCE_UPDATED = "notifications/resources/updated";
        public static final String RESOURCE_LIST_CHANGED = "notifications/resources/list_changed";
        public static final String TOOL_LIST_CHANGED = "notifications/tools/list_changed";
        public static final String PROMPT_LIST_CHANGED = "notifications/prompts/list_changed";
        public static final String CANCELLED = "notifications/cancelled";
        public static final String ROOTS_LIST_CHANGED = "notifications/roots/list_changed";
    }
    
    // Factory methods for common notifications
    public static MCPNotification progress(String progressToken, Double progress, Integer total) {
        var notification = new MCPNotification(Methods.PROGRESS);
        // Would need to construct proper params JsonNode here
        return notification;
    }
    
    public static MCPNotification message(String level, String logger, String data) {
        var notification = new MCPNotification(Methods.MESSAGE);
        // Would need to construct proper params JsonNode here
        return notification;
    }
    
    public static MCPNotification resourceUpdated(String uri) {
        var notification = new MCPNotification(Methods.RESOURCE_UPDATED);
        // Would need to construct proper params JsonNode here
        return notification;
    }
    
    public static MCPNotification resourceListChanged() {
        return new MCPNotification(Methods.RESOURCE_LIST_CHANGED);
    }
    
    public static MCPNotification toolListChanged() {
        return new MCPNotification(Methods.TOOL_LIST_CHANGED);
    }
    
    public static MCPNotification promptListChanged() {
        return new MCPNotification(Methods.PROMPT_LIST_CHANGED);
    }
    
    public static MCPNotification cancelled(String requestId, String reason) {
        var notification = new MCPNotification(Methods.CANCELLED);
        // Would need to construct proper params JsonNode here
        return notification;
    }
    
    public static MCPNotification rootsListChanged() {
        return new MCPNotification(Methods.ROOTS_LIST_CHANGED);
    }
}
