package tech.kayys.wayang.node.websearch.provider.bing;

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
public class BingSearchProvider implements SearchProvider {

    private static final String DEFAULT_WEB_ENDPOINT = "https://api.bing.microsoft.com/v7.0/search";
    private static final String DEFAULT_IMAGE_ENDPOINT = "https://api.bing.microsoft.com/v7.0/images/search";
    private static final String DEFAULT_VIDEO_ENDPOINT = "https://api.bing.microsoft.com/v7.0/videos/search";
    private static final String DEFAULT_NEWS_ENDPOINT = "https://api.bing.microsoft.com/v7.0/news/search";

    @Inject
    ProviderHttpClient httpClient;

    @Override
    public String getProviderId() { return "bing"; }

    @Override
    public String getProviderName() { return "Bing Search"; }

    @Override
    public Set<SearchCapability> getSupportedCapabilities() {
        return Set.of(SearchCapability.TEXT_SEARCH, SearchCapability.IMAGE_SEARCH, 
                     SearchCapability.VIDEO_SEARCH, SearchCapability.NEWS_SEARCH);
    }

    @Override
    public Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config) {
        String apiKey = config.get("api-key");
        if (apiKey == null || apiKey.isBlank()) {
            return Uni.createFrom().failure(new ProviderException(getProviderId(), "Missing config 'api-key'"));
        }

        long timeoutMs = Math.max(200L, config.getLong("timeout-ms", 2500L));
        URI uri = buildUri(request, config);
        return httpClient.getJson(
                getProviderId(),
                uri,
                Map.of("Ocp-Apim-Subscription-Key", apiKey),
                timeoutMs
            )
            .map(json -> toProviderResult(request.getCapability(), json));
    }

    private URI buildUri(SearchRequest request, ProviderConfig config) {
        Map<String, String> params = new HashMap<>();
        params.put("q", request.query());
        params.put("count", String.valueOf(Math.min(request.maxResults(), 50)));
        params.put("mkt", localeToMarket(request.locale()));
        params.put("safeSearch", request.safeSearch() ? "Moderate" : "Off");
        params.put("offset", "0");

        String endpoint = config.get("endpoint");
        String base = resolveEndpoint(request.getCapability(), endpoint);
        return URI.create(base + "?" + queryString(params));
    }

    private String resolveEndpoint(SearchCapability capability, String endpoint) {
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.trim();
        }
        return switch (capability) {
            case IMAGE_SEARCH -> DEFAULT_IMAGE_ENDPOINT;
            case VIDEO_SEARCH -> DEFAULT_VIDEO_ENDPOINT;
            case NEWS_SEARCH -> DEFAULT_NEWS_ENDPOINT;
            default -> DEFAULT_WEB_ENDPOINT;
        };
    }

    private ProviderSearchResult toProviderResult(SearchCapability capability, JsonNode root) {
        JsonNode values = switch (capability) {
            case IMAGE_SEARCH, VIDEO_SEARCH, NEWS_SEARCH -> root.path("value");
            default -> root.path("webPages").path("value");
        };

        List<SearchResult> results = new ArrayList<>();
        if (values.isArray()) {
            int idx = 0;
            for (JsonNode item : values) {
                String url = item.path("url").asText("");
                if (url.isBlank()) {
                    continue;
                }
                String title = item.path("name").asText("");
                if (title.isBlank()) {
                    title = item.path("title").asText(url);
                }
                String snippet = item.path("snippet").asText("");
                if (snippet.isBlank()) {
                    snippet = item.path("description").asText("");
                }

                results.add(SearchResult.builder()
                    .title(title)
                    .url(url)
                    .snippet(snippet)
                    .source(getProviderId())
                    .score(Math.max(1.0, 100.0 - (idx++ * 2.0)))
                    .build());
            }
        }

        int total = parseTotal(root.path("webPages").path("totalEstimatedMatches"));
        if (total == 0) {
            total = parseTotal(root.path("totalEstimatedMatches"));
        }
        return ProviderSearchResult.builder()
            .providerId(getProviderId())
            .results(results)
            .totalResults(total)
            .durationMs(0L)
            .build();
    }

    private int parseTotal(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return 0;
        }
        if (node.isNumber()) {
            long value = node.asLong();
            return (int) Math.min(value, Integer.MAX_VALUE);
        }
        try {
            long value = Long.parseLong(node.asText("0"));
            return (int) Math.min(value, Integer.MAX_VALUE);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String localeToMarket(String locale) {
        String value = locale == null || locale.isBlank() ? "en-US" : locale.trim();
        return value.contains("-") ? value : value + "-US";
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
    public int getPriority() { return 90; }
}
