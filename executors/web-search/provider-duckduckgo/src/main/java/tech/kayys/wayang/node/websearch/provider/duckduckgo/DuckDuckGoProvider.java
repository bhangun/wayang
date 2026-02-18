package tech.kayys.wayang.node.websearch.provider.duckduckgo;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.node.websearch.api.*;
import tech.kayys.wayang.node.websearch.spi.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DuckDuckGoProvider implements SearchProvider {

    private static final String DEFAULT_ENDPOINT = "https://api.duckduckgo.com/";

    @Inject
    ProviderHttpClient httpClient;

    @Override
    public String getProviderId() { return "duckduckgo"; }

    @Override
    public String getProviderName() { return "DuckDuckGo"; }

    @Override
    public Set<SearchCapability> getSupportedCapabilities() {
        return Set.of(SearchCapability.TEXT_SEARCH);
    }

    @Override
    public Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config) {
        long timeoutMs = Math.max(200L, config.getLong("timeout-ms", 2500L));
        URI uri = buildUri(request, config);
        return httpClient.getJson(getProviderId(), uri, Map.of(), timeoutMs)
            .map(json -> toProviderResult(request.maxResults(), json));
    }

    private URI buildUri(SearchRequest request, ProviderConfig config) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("q", request.query());
        params.put("format", "json");
        params.put("no_redirect", "1");
        params.put("no_html", "1");
        params.put("skip_disambig", "1");
        params.put("kl", request.locale());

        String endpoint = config.get("endpoint");
        String base = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();
        return URI.create(base + "?" + queryString(params));
    }

    private ProviderSearchResult toProviderResult(int maxResults, JsonNode root) {
        List<SearchResult> results = new ArrayList<>();
        JsonNode topics = root.path("RelatedTopics");
        if (topics.isArray()) {
            collectRelatedTopics(topics, results, maxResults);
        }

        if (results.isEmpty()) {
            String abstractUrl = root.path("AbstractURL").asText("");
            if (!abstractUrl.isBlank()) {
                results.add(SearchResult.builder()
                    .title(root.path("Heading").asText(abstractUrl))
                    .url(abstractUrl)
                    .snippet(root.path("AbstractText").asText(""))
                    .source(getProviderId())
                    .score(100.0)
                    .build());
            }
        }

        return ProviderSearchResult.builder()
            .providerId(getProviderId())
            .results(results)
            .totalResults(results.size())
            .durationMs(0L)
            .build();
    }

    private void collectRelatedTopics(JsonNode node, List<SearchResult> results, int maxResults) {
        for (JsonNode item : node) {
            if (results.size() >= maxResults) {
                return;
            }
            if (item.has("Topics")) {
                collectRelatedTopics(item.path("Topics"), results, maxResults);
                continue;
            }
            String url = item.path("FirstURL").asText("");
            if (url.isBlank()) {
                continue;
            }
            String text = item.path("Text").asText(url);
            String title = text.contains(" - ") ? text.substring(0, text.indexOf(" - ")) : text;
            results.add(SearchResult.builder()
                .title(title)
                .url(url)
                .snippet(text)
                .source(getProviderId())
                .score(Math.max(1.0, 100.0 - (results.size() * 2.0)))
                .build());
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
    public int getPriority() { return 70; }
}
