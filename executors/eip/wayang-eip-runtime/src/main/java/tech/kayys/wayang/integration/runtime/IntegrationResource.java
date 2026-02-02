package io.wayang.executors.integration.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/integration")
@ApplicationScoped
public class IntegrationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/health")
    public String health() {
        return "Integration Runtime is running";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info")
    public String info() {
        return "{\"status\":\"running\",\"modules\":\"aws,salesforce,azure,gcp,kafka,mongodb,postgresql,redis\"}";
    }
}