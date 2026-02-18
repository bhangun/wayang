package tech.kayys.gamelan.executor.rag.langchain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/observability/slo/config")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AdminProtected
public class RagSloConfigAdminResource {

    @Inject
    RagSloConfigAdminService service;

    @GET
    public RagSloConfigStatus status() {
        return service.status();
    }

    @PUT
    public RagSloConfigStatus update(RagSloConfigUpdate update) {
        return service.update(update);
    }

    @POST
    @Path("/reload")
    public RagSloConfigStatus reload() {
        return service.reload();
    }
}
