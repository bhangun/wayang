package tech.kayys.wayang.network.capability;

import java.util.concurrent.CompletionStage;

public interface CapabilityNegotiator {

    CompletionStage<CapabilityMatch> negotiate(CapabilityRequirement requirement, Object remoteCapabilities);
}
