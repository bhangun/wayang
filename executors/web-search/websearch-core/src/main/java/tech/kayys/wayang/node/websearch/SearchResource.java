package tech.kayys.wayang.node.websearch;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import tech.kayys.gamelan.engine.error.ErrorCode;
import tech.kayys.gamelan.engine.error.GamelanException;
import tech.kayys.wayang.node.websearch.api.*;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class SearchResource {
    
    @Inject
    SearchOrchestrator orchestrator;

    @ConfigProperty(name = "wayang.websearch.search.fail-mode", defaultValue = "strict")
    String failMode;

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

        Uni<SearchResponse> searchUni = orchestrator.search(request);
        if (isFallbackMode()) {
            return searchUni
                .onFailure().recoverWithItem(ignored -> standaloneFallback(query));
        }

        return searchUni.onItem().transform(response -> {
            String providerUsed = response.providerUsed();
            if (providerUsed == null) {
                throw new GamelanException(
                    ErrorCode.SEARCH_PROVIDER_UNAVAILABLE,
                    "No search provider available");
            }
            String normalizedProvider = providerUsed.trim().toLowerCase(Locale.ROOT);
            if ("fallback".equals(normalizedProvider) || "none".equals(normalizedProvider)) {
                throw new GamelanException(
                    ErrorCode.SEARCH_PROVIDER_UNAVAILABLE,
                    "No search provider available");
            }
            return response;
        });
    }

    private boolean isFallbackMode() {
        return "fallback".equalsIgnoreCase(failMode);
    }

    private static SearchResponse standaloneFallback(String query) {
        if (query == null || query.isBlank()) {
            return SearchResponse.builder()
                .results(List.of())
                .totalResults(0)
                .providerUsed("none")
                .durationMs(0L)
                .build();
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        SearchResult fallback = SearchResult.builder()
            .title("Search web for '" + query + "'")
            .url("https://duckduckgo.com/?q=" + encodedQuery)
            .snippet("Live providers are unavailable. Open this URL to view web results directly.")
            .source("fallback")
            .score(50.0)
            .build();

        return SearchResponse.builder()
            .results(List.of(fallback))
            .totalResults(1)
            .providerUsed("fallback")
            .durationMs(0L)
            .build();
    }
}
