package tech.kayys.wayang.node.websearch.provider.bing;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.spi.ProviderConfig;
import tech.kayys.wayang.node.websearch.spi.ProviderHttpClient;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BingSearchProviderTest {

    @Test
    void searchMapsWebResultsAndSendsSubscriptionKey() throws Exception {
        ProviderHttpClient httpClient = mock(ProviderHttpClient.class);
        BingSearchProvider provider = new BingSearchProvider();
        provider.httpClient = httpClient;

        var json = new ObjectMapper().readTree("""
            {
              "webPages": {
                "totalEstimatedMatches": 42,
                "value": [
                  {"name":"Bing Result","url":"https://example.com/b","snippet":"bing snippet"}
                ]
              }
            }
            """);
        when(httpClient.getJson(any(), any(), anyMap(), anyLong()))
            .thenReturn(Uni.createFrom().item(json));

        SearchRequest request = SearchRequest.builder()
            .query("workflow")
            .searchType("text")
            .maxResults(10)
            .locale("en-US")
            .build();
        ProviderConfig config = ProviderConfig.forProvider("bing", Map.of("api-key", "bing-key"));

        var response = provider.search(request, config).await().indefinitely();

        assertEquals("bing", response.providerId());
        assertEquals(42, response.totalResults());
        assertEquals(1, response.results().size());
        assertEquals("https://example.com/b", response.results().get(0).url());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> headersCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(httpClient).getJson(any(), uriCaptor.capture(), headersCaptor.capture(), anyLong());

        assertEquals("bing-key", headersCaptor.getValue().get("Ocp-Apim-Subscription-Key"));
        assertTrue(uriCaptor.getValue().toString().contains("q=workflow"));
    }
}
