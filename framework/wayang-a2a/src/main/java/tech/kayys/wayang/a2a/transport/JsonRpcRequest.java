package tech.kayys.wayang.a2a.transport;

import java.util.Map;

/**
 * Represents a standard JSON-RPC 2.0 request.
 */
public record JsonRpcRequest(
    String jsonrpc,
    String method,
    Map<String, Object> params,
    String id
) {
    public JsonRpcRequest {
        if (jsonrpc == null || !jsonrpc.equals("2.0")) {
            throw new IllegalArgumentException("jsonrpc must be '2.0'");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method is required");
        }
    }
    
    public static JsonRpcRequest create(String method, Map<String, Object> params, String id) {
        return new JsonRpcRequest("2.0", method, params, id);
    }
}
