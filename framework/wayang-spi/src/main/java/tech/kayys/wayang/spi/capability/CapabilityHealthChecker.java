package tech.kayys.wayang.spi.capability;

public interface CapabilityHealthChecker {

    CapabilityProviderStatus check(CapabilityProviderRegistration provider);
}
