package tech.kayys.wayang.a2a.transport;

/**
 * Represents a standard JSON-RPC 2.0 response.
 */
public record JsonRpcResponse(
    String jsonrpc,
    Object result,
    JsonRpcError error,
    String id
) {
    public JsonRpcResponse {
        if (jsonrpc == null || !jsonrpc.equals("2.0")) {
            throw new IllegalArgumentException("jsonrpc must be '2.0'");
        }
        if (result != null && error != null) {
            throw new IllegalArgumentException("Cannot have both result and error");
        }
    }
    
    public static JsonRpcResponse success(Object result, String id) {
        return new JsonRpcResponse("2.0", result, null, id);
    }
    
    public static JsonRpcResponse error(JsonRpcError error, String id) {
        return new JsonRpcResponse("2.0", null, error, id);
    }
}
