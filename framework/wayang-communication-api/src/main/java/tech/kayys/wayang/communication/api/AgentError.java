package tech.kayys.wayang.communication.api;

public record AgentError(
        String code,
        String message,
        boolean retryable
) {
    public static AgentError of(String code, String message) {
        return new AgentError(code, message, false);
    }

    public static AgentError retryable(String code, String message) {
        return new AgentError(code, message, true);
    }
}
