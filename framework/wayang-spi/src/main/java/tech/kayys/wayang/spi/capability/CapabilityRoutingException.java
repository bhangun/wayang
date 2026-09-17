package tech.kayys.wayang.spi.capability;

public class CapabilityRoutingException extends RuntimeException {

    public CapabilityRoutingException(String message) {
        super(message);
    }

    public CapabilityRoutingException(String message, Throwable cause) {
        super(message, cause);
    }
}
