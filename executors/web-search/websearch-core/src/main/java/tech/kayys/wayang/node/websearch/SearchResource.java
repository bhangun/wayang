package tech.kayys.wayang.node.websearch;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import tech.kayys.wayang.node.websearch.api.*;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    
    @Inject
    SearchOrchestrator orchestrator;

    @GET
    public Uni<SearchResponse> search(
            @QueryParam("q") String query,
            @QueryParam("type") @DefaultValue("text") String type,
            @QueryParam("max") @DefaultValue("10") int maxResults) {
        
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Query 'q' is required");
        }
        if (maxResults < 1 || maxResults > 100) {
            throw new BadRequestException("Query param 'max' must be between 1 and 100");
        }
        
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .searchType(type)
            .maxResults(maxResults)
            .build();
        
        return orchestrator.search(request);
    }
}
