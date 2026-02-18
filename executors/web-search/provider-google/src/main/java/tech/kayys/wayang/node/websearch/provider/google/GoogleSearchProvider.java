package tech.kayys.wayang.node.websearch.provider.google;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.node.websearch.api.*;
import tech.kayys.wayang.node.websearch.exception.ProviderException;
import tech.kayys.wayang.node.websearch.spi.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class GoogleSearchProvider implements SearchProvider {

    private static final String DEFAULT_ENDPOINT = "https://customsearch.googleapis.com/customsearch/v1";

    @Inject
    ProviderHttpClient httpClient;

    @Override
    public String getProviderId() { return "google"; }

    @Override
    public String getProviderName() { return "Google Search"; }

    @Override
    public Set<SearchCapability> getSupportedCapabilities() {
        return Set.of(SearchCapability.TEXT_SEARCH, SearchCapability.IMAGE_SEARCH, SearchCapability.NEWS_SEARCH);
    }

    @Override
    public Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config) {
        String apiKey = config.get("api-key");
        String searchEngineId = config.get("search-engine-id");
        if (apiKey == null || apiKey.isBlank()) {
            return Uni.createFrom().failure(new ProviderException(getProviderId(), "Missing config 'api-key'"));
        }
        if (searchEngineId == null || searchEngineId.isBlank()) {
            return Uni.createFrom().failure(new ProviderException(getProviderId(), "Missing config 'search-engine-id'"));
        }

        long timeoutMs = Math.max(200L, config.getLong("timeout-ms", 2500L));
        URI uri = buildUri(request, config, apiKey, searchEngineId);

        return httpClient.getJson(getProviderId(), uri, Map.of(), timeoutMs)
            .map(this::toProviderResult);
    }

    private URI buildUri(SearchRequest request, ProviderConfig config, String apiKey, String searchEngineId) {
        Map<String, String> params = new HashMap<>();
        params.put("key", apiKey);
        params.put("cx", searchEngineId);
        params.put("q", request.query());
        params.put("num", String.valueOf(Math.min(request.maxResults(), 10)));
        params.put("safe", request.safeSearch() ? "active" : "off");
        params.put("hl", request.locale());

        if (request.getCapability() == SearchCapability.IMAGE_SEARCH) {
            params.put("searchType", "image");
        }

        String endpoint = config.get("endpoint");
        String base = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();
        return URI.create(base + "?" + queryString(params));
    }

    private ProviderSearchResult toProviderResult(JsonNode root) {
        List<SearchResult> results = new ArrayList<>();
        JsonNode items = root.path("items");
        if (items.isArray()) {
            int idx = 0;
            for (JsonNode item : items) {
                String title = item.path("title").asText("");
                String url = item.path("link").asText("");
                if (url.isBlank()) {
                    continue;
                }
                results.add(SearchResult.builder()
                    .title(title.isBlank() ? url : title)
                    .url(url)
                    .snippet(item.path("snippet").asText(""))
                    .source(getProviderId())
                    .score(Math.max(1.0, 100.0 - (idx++ * 2.0)))
                    .build());
            }
        }

        int totalResults = parseTotal(root.path("searchInformation").path("totalResults").asText("0"));
        return ProviderSearchResult.builder()
            .providerId(getProviderId())
            .results(results)
            .totalResults(totalResults)
            .durationMs(0L)
            .build();
    }

    private int parseTotal(String value) {
        try {
            long parsed = Long.parseLong(value);
            return (int) Math.min(parsed, Integer.MAX_VALUE);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String queryString(Map<String, String> params) {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            entries.add(
                URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
            );
        }
        return String.join("&", entries);
    }

    @Override
    public int getPriority() { return 100; }
}
