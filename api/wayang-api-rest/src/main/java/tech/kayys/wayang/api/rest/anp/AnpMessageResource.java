package tech.kayys.wayang.api.rest.anp;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.anp.auth.AnpAuthenticator;
import tech.kayys.wayang.anp.auth.AnpAuthVerifier;
import tech.kayys.wayang.anp.auth.AnpHttpSignature;
import tech.kayys.wayang.anp.config.AnpProtocolConfig;
import tech.kayys.wayang.anp.identity.AnpIdentityKeyPair;
import tech.kayys.wayang.anp.identity.AnpKeyStore;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.anp.security.AnpSecurityMapper;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.util.Map;
import java.util.Optional;

/**
 * REST Endpoint for ANP 1.1 inbound agent messaging and protocol dispatch.
 */
@Path("/api/v1/anp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnpMessageResource {

    @Inject
    AnpProtocolConfig config;

    @Inject
    AnpAuthVerifier authVerifier;

    @Inject
    AnpAuthenticator authenticator;

    @Inject
    AnpKeyStore keyStore;

    @Inject
    AnpSecurityMapper securityMapper;

    @Inject
    AnpWellKnownResource wellKnownResource;

    /**
     * Inbound message reception endpoint.
     * Receives ANP or A2A-over-ANP invocations from peer agents.
     */
    @POST
    @Path("/message")
    public Response handleMessage(
            @HeaderParam("Signature") String signatureHeader,
            @HeaderParam("Authorization") String authHeader,
            @HeaderParam("Host") String hostHeader,
            AnpMessageRequest request) {

        if (!config.enabled()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "ANP protocol adapter is currently disabled on this node"))
                    .build();
        }

        if (request == null || request.senderDid() == null || request.senderDid().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing required field: senderDid"))
                    .build();
        }

        // 1. Signature Verification
        String rawSig = signatureHeader != null ? signatureHeader : authHeader;
        AnpHttpSignature parsedSignature = null;
        if (rawSig != null && !rawSig.isBlank()) {
            try {
                parsedSignature = AnpHttpSignature.parseHeader(rawSig.replaceFirst("(?i)^Signature\\s+", ""));
                String host = hostHeader != null ? hostHeader : config.domain();
                boolean valid = authVerifier.verify("POST", "/api/v1/anp/message", host, parsedSignature);
                if (!valid && config.requireSignature()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("error", "Invalid or untrusted ANP HTTP signature"))
                            .build();
                }
            } catch (Exception e) {
                if (config.requireSignature()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("error", "Failed to parse signature: " + e.getMessage()))
                            .build();
                }
            }
        } else if (config.requireSignature()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "ANP HTTP Signature required for authentication"))
                    .build();
        }

        // 2. Map caller identity and security context
        DidWbaIdentity callerDid = DidWbaIdentity.parse(request.senderDid());
        SecurityContextSnapshot securityContext = securityMapper.map(
                callerDid,
                parsedSignature != null ? parsedSignature : new AnpHttpSignature("anonymous", "none", "", new byte[0]),
                request.tenantId(),
                Optional.empty()
        );

        // 3. Process the agent message
        String outputPayload = "Processed ANP message from " + request.senderDid() + " [" + request.payload() + "]";
        AgentResponse agentResponse = AgentResponse.success(
                MessagePayload.text(outputPayload),
                Map.of("principal", securityContext.principal().id(), "protocol", request.protocol() != null ? request.protocol() : "a2a/1.0")
        );

        // 4. Construct response DTO
        AnpMessageResponse responseDto = new AnpMessageResponse(
                true,
                outputPayload,
                agentResponse.metadata(),
                null
        );

        // 5. Sign response if local signing key is configured
        var responseBuilder = Response.ok(responseDto);
        Optional<String> agentKeyId = keyStore.getAgentKeyId("default");
        if (agentKeyId.isPresent()) {
            keyStore.getKey(agentKeyId.get()).ifPresent(kp -> {
                AnpHttpSignature respSig = authenticator.sign("POST", "/api/v1/anp/message", config.domain(), kp);
                responseBuilder.header("Signature", respSig.toHeaderValue());
            });
        }

        return responseBuilder.build();
    }

    @GET
    @Path("/status")
    public Response status() {
        return Response.ok(Map.of(
                "protocol", "ANP",
                "version", "1.1",
                "enabled", config.enabled(),
                "domain", config.domain(),
                "preferA2A", config.preferA2A(),
                "requireSignature", config.requireSignature()
        )).build();
    }

    @GET
    @Path("/agent.json")
    public Response agentJson(@QueryParam("did") String did) {
        return wellKnownResource.getAgentDescription(did);
    }

    @GET
    @Path("/did.json")
    public Response didJson(@QueryParam("did") String did) {
        return wellKnownResource.getDidDocument(did);
    }

    public record AnpMessageRequest(
            String senderDid,
            String targetDid,
            String protocol,
            String payload,
            String tenantId,
            Map<String, Object> metadata
    ) {}

    public record AnpMessageResponse(
            boolean success,
            String payload,
            Map<String, Object> metadata,
            String error
    ) {}
}
