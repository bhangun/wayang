package tech.kayys.wayang.node.websearch.api;

import java.util.List;

public record SearchResponse(
    List<SearchResult> results,
    int totalResults,
    String providerUsed,
    long durationMs
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<SearchResult> results;
        private int totalResults;
        private String providerUsed;
        private long durationMs;
        
        public Builder results(List<SearchResult> results) { this.results = results; return this; }
        public Builder totalResults(int totalResults) { this.totalResults = totalResults; return this; }
        public Builder providerUsed(String providerUsed) { this.providerUsed = providerUsed; return this; }
        public Builder durationMs(long durationMs) { this.durationMs = durationMs; return this; }
        
        public SearchResponse build() {
            return new SearchResponse(results, totalResults, providerUsed, durationMs);
        }
    }
}
