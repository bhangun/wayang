package tech.kayys.wayang.node.websearch.api;

import java.util.List;
import java.util.Locale;

public record SearchRequest(
    String query,
    String searchType,
    int maxResults,
    List<String> providers,
    String locale,
    boolean safeSearch,
    String tenantId
) {
    public SearchRequest {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query required");
        }
        query = query.trim();

        if (searchType == null || searchType.isBlank()) {
            searchType = "text";
        } else {
            searchType = searchType.trim().toLowerCase(Locale.ROOT);
        }

        if (maxResults < 1 || maxResults > 100) {
            throw new IllegalArgumentException("maxResults must be 1-100");
        }

        providers = providers == null || providers.isEmpty()
            ? List.of("google", "bing", "duckduckgo")
            : List.copyOf(providers);

        locale = locale == null || locale.isBlank() ? "en" : locale.trim();
        tenantId = tenantId == null || tenantId.isBlank() ? null : tenantId.trim();
    }
    
    public SearchCapability getCapability() {
        return SearchCapability.fromString(searchType);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String query;
        private String searchType = "text";
        private int maxResults = 10;
        private List<String> providers = List.of("google", "bing", "duckduckgo");
        private String locale = "en";
        private boolean safeSearch = true;
        private String tenantId;
        
        public Builder query(String query) { this.query = query; return this; }
        public Builder searchType(String searchType) { this.searchType = searchType; return this; }
        public Builder maxResults(int maxResults) { this.maxResults = maxResults; return this; }
        public Builder providers(List<String> providers) { this.providers = providers; return this; }
        public Builder locale(String locale) { this.locale = locale; return this; }
        public Builder safeSearch(boolean safeSearch) { this.safeSearch = safeSearch; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        
        public SearchRequest build() {
            return new SearchRequest(query, searchType, maxResults, providers, locale, safeSearch, tenantId);
        }
    }
}
