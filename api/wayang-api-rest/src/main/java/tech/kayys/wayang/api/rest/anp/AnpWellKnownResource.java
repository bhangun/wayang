package tech.kayys.wayang.api.rest.anp;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.anp.config.AnpProtocolConfig;
import tech.kayys.wayang.anp.description.AnpAgentDescription;
import tech.kayys.wayang.anp.description.AnpAgentDescriptionService;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.anp.identity.DidWbaResolver;

import java.util.Map;
import java.util.Optional;

/**
 * Standard W3C & ANP 1.1 Discovery endpoints located at {@code /.well-known/*}.
 */
@Path("/.well-known")
@Produces(MediaType.APPLICATION_JSON)
public class AnpWellKnownResource {

    @Inject
    AnpAgentDescriptionService descriptionService;

    @Inject
    DidWbaResolver didResolver;

    @Inject
    AnpProtocolConfig config;

    /**
     * Agent Description Protocol (ADP) endpoint.
     * Spec 07: GET /.well-known/agent.json
     */
    @GET
    @Path("/agent.json")
    public Response getAgentDescription(@QueryParam("did") String did) {
        String targetDid = (did != null && !did.isBlank())
                ? did
                : "did:wba:" + config.domain() + ":wayang";

        Optional<AnpAgentDescription> desc = descriptionService.findByDid(targetDid);
        if (desc.isPresent()) {
            return Response.ok(desc.get()).build();
        }

        // Return default self description if none explicitly registered for target
        var all = descriptionService.all();
        if (!all.isEmpty()) {
            return Response.ok(all.iterator().next()).build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Agent description not found for: " + targetDid))
                .build();
    }

    /**
     * Alternative ADP endpoint without .json extension.
     */
    @GET
    @Path("/agent")
    public Response getAgentDescriptionPlain(@QueryParam("did") String did) {
        return getAgentDescription(did);
    }

    /**
     * W3C DID:WBA Document endpoint.
     * Spec 03: GET /.well-known/did.json
     */
    @GET
    @Path("/did.json")
    public Response getDidDocument(@QueryParam("did") String did) {
        String targetDid = (did != null && !did.isBlank())
                ? did
                : "did:wba:" + config.domain() + ":wayang";

        DidWbaIdentity identity = DidWbaIdentity.parse(targetDid);
        return didResolver.resolve(identity).toCompletableFuture().join()
                .map(doc -> Response.ok(doc).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "DID document not found for: " + targetDid))
                        .build());
    }
}
