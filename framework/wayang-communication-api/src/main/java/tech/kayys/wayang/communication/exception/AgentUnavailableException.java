package tech.kayys.wayang.communication.exception;

public final class AgentUnavailableException
        extends CommunicationException {

    public AgentUnavailableException(
            String message
    ) {
        super(message);
    }

    public AgentUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
