package tech.kayys.wayang.node.websearch.spi;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.node.websearch.api.*;
import java.util.Set;

public interface SearchProvider {
    String getProviderId();
    String getProviderName();
    Set<SearchCapability> getSupportedCapabilities();
    Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config);
    int getPriority();
    default boolean isEnabled() { return true; }
}
