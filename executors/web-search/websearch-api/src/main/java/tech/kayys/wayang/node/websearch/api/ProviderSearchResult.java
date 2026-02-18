package tech.kayys.wayang.node.websearch.api;

import java.util.List;

public record ProviderSearchResult(
    String providerId,
    List<SearchResult> results,
    int totalResults,
    long durationMs
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String providerId;
        private List<SearchResult> results;
        private int totalResults;
        private long durationMs;
        
        public Builder providerId(String providerId) { this.providerId = providerId; return this; }
        public Builder results(List<SearchResult> results) { this.results = results; return this; }
        public Builder totalResults(int totalResults) { this.totalResults = totalResults; return this; }
        public Builder durationMs(long durationMs) { this.durationMs = durationMs; return this; }
        
        public ProviderSearchResult build() {
            return new ProviderSearchResult(providerId, results, totalResults, durationMs);
        }
    }
}
