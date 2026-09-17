package tech.kayys.wayang.spi.capability.event;

@FunctionalInterface
public interface CapabilityEventListener {

    void onEvent(CapabilityEvent event);
}
