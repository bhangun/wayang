package tech.kayys.wayang.spi.capability.event;

public interface CapabilityEventPublisher {

    void publish(CapabilityEvent event);

    void addListener(CapabilityEventListener listener);

    void removeListener(CapabilityEventListener listener);
}
