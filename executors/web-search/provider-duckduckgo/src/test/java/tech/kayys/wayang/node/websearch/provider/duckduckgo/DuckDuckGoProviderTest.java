package tech.kayys.wayang.node.websearch.provider.duckduckgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.spi.ProviderConfig;
import tech.kayys.wayang.node.websearch.spi.ProviderHttpClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DuckDuckGoProviderTest {

    @Test
    void searchMapsNestedRelatedTopics() throws Exception {
        ProviderHttpClient httpClient = mock(ProviderHttpClient.class);
        DuckDuckGoProvider provider = new DuckDuckGoProvider();
        provider.httpClient = httpClient;

        var json = new ObjectMapper().readTree("""
            {
              "RelatedTopics": [
                {
                  "Topics": [
                    {"FirstURL":"https://example.com/ddg-a","Text":"Alpha - Description"}
                  ]
                },
                {"FirstURL":"https://example.com/ddg-b","Text":"Beta - Description"}
              ]
            }
            """);
        when(httpClient.getJson(any(), any(), anyMap(), anyLong()))
            .thenReturn(Uni.createFrom().item(json));

        SearchRequest request = SearchRequest.builder()
            .query("wayang")
            .maxResults(1)
            .build();
        ProviderConfig config = ProviderConfig.forProvider("duckduckgo", Map.of());

        var response = provider.search(request, config).await().indefinitely();

        assertEquals("duckduckgo", response.providerId());
        assertEquals(1, response.results().size());
        assertEquals("Alpha", response.results().get(0).title());
        assertTrue(response.results().get(0).url().contains("ddg-a"));
    }
}
