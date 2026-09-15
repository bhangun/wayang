package tech.kayys.wayang.network.exception;

public class AgentNetworkException extends RuntimeException {
    public AgentNetworkException(String message) { super(message); }
    public AgentNetworkException(String message, Throwable cause) { super(message, cause); }
}
