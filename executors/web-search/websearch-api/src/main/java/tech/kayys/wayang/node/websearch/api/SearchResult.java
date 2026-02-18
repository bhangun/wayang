package tech.kayys.wayang.node.websearch.api;

import java.util.Map;

public record SearchResult(
    String title,
    String url,
    String snippet,
    String source,
    Double score,
    Map<String, Object> metadata
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String title;
        private String url;
        private String snippet;
        private String source;
        private Double score;
        private Map<String, Object> metadata = Map.of();
        
        public Builder title(String title) { this.title = title; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder snippet(String snippet) { this.snippet = snippet; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder score(Double score) { this.score = score; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        
        public SearchResult build() {
            return new SearchResult(title, url, snippet, source, score, metadata);
        }
    }
}
