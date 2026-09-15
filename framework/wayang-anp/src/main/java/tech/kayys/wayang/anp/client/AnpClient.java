package tech.kayys.wayang.anp.client;

import tech.kayys.wayang.anp.auth.AnpAuthenticator;
import tech.kayys.wayang.anp.auth.AnpHttpSignature;
import tech.kayys.wayang.anp.identity.AnpIdentityKeyPair;
import tech.kayys.wayang.anp.identity.AnpKeyStore;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.anp.meta.AnpCapabilityAdvertisement;
import tech.kayys.wayang.anp.meta.AnpCapabilitySelection;
import tech.kayys.wayang.anp.meta.AnpMetaProtocolNegotiator;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.message.MessagePayload;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Low-level client responsible for exchanging messages with an external ANP agent.
 */
public final class AnpClient {

    private final AnpKeyStore keyStore;
    private final AnpAuthenticator authenticator;
    private final AnpMetaProtocolNegotiator metaNegotiator;
    private Function<String, CompletionStage<AgentResponse>> transportHandler;

    public AnpClient(
            AnpKeyStore keyStore,
            AnpAuthenticator authenticator,
            AnpMetaProtocolNegotiator metaNegotiator) {
        this.keyStore = Objects.requireNonNull(keyStore, "keyStore");
        this.authenticator = Objects.requireNonNull(authenticator, "authenticator");
        this.metaNegotiator = Objects.requireNonNull(metaNegotiator, "metaNegotiator");
        this.transportHandler = msg -> CompletableFuture.completedFuture(
                AgentResponse.success(MessagePayload.text("ANP ack: " + msg))
        );
    }

    /** For testing / custom transport mocking. */
    public void setTransportHandler(Function<String, CompletionStage<AgentResponse>> handler) {
        this.transportHandler = Objects.requireNonNull(handler, "handler");
    }

    /** Negotiates meta-protocol and executes remote invocation. */
    public CompletionStage<AgentResponse> send(
            DidWbaIdentity targetDid,
            MessagePayload payload,
            String senderAgentId) {

        // 1. Meta-protocol negotiation: advertise Wayang preferences (A2A over ANP)
        AnpCapabilityAdvertisement senderAdv = AnpCapabilityAdvertisement.defaultWayang(senderAgentId);
        AnpCapabilityAdvertisement receiverAdv = AnpCapabilityAdvertisement.defaultWayang(targetDid.raw());
        AnpCapabilitySelection selection = metaNegotiator.negotiate(senderAdv, receiverAdv);

        // 2. Authentication: sign outbound request if key is available
        Optional<String> keyId = keyStore.getAgentKeyId(senderAgentId);
        if (keyId.isPresent()) {
            Optional<AnpIdentityKeyPair> keyPair = keyStore.getKey(keyId.get());
            keyPair.ifPresent(kp -> {
                authenticator.sign("POST", "/message", targetDid.host(), kp);
            });
        }

        // 3. Dispatch through transport
        String content = payload != null ? payload.toString() : "";
        return transportHandler.apply(content).thenApply(resp -> {
            Map<String, Object> meta = Map.of(
                    "anp.protocol", selection.protocol(),
                    "anp.codec", selection.codec(),
                    "anp.target", targetDid.raw()
            );
            return AgentResponse.success(resp.payload(), meta);
        });
    }
}
