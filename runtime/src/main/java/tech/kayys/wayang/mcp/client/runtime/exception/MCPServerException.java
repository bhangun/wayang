package tech.kayys.wayang.mcp.client.runtime.exception;


class MCPServerException extends Exception {
    private final int errorCode;
    
    public MCPServerException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
