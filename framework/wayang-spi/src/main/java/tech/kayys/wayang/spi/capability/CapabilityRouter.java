package tech.kayys.wayang.spi.capability;

public interface CapabilityRouter {

    CapabilityRoute route(CapabilityRoutingRequest request) throws CapabilityRoutingException;
}
