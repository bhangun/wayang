package tech.kayys.wayang.node.websearch.provider.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.exception.ProviderException;
import tech.kayys.wayang.node.websearch.spi.ProviderConfig;
import tech.kayys.wayang.node.websearch.spi.ProviderHttpClient;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleSearchProviderTest {

    @Test
    void searchMapsGoogleResponseAndBuildsRequest() throws Exception {
        ProviderHttpClient httpClient = mock(ProviderHttpClient.class);
        GoogleSearchProvider provider = new GoogleSearchProvider();
        provider.httpClient = httpClient;

        var json = new ObjectMapper().readTree("""
            {
              "searchInformation": { "totalResults": "123" },
              "items": [
                {"title":"First","link":"https://example.com/a","snippet":"snippet a"}
              ]
            }
            """);
        when(httpClient.getJson(any(), any(), anyMap(), anyLong()))
            .thenReturn(Uni.createFrom().item(json));

        SearchRequest request = SearchRequest.builder()
            .query("openai")
            .searchType("text")
            .maxResults(5)
            .build();
        ProviderConfig config = ProviderConfig.forProvider("google", Map.of(
            "api-key", "g-key",
            "search-engine-id", "cx-123"
        ));

        var response = provider.search(request, config).await().indefinitely();

        assertEquals("google", response.providerId());
        assertEquals(123, response.totalResults());
        assertEquals(1, response.results().size());
        assertEquals("https://example.com/a", response.results().get(0).url());

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(httpClient).getJson(any(), uriCaptor.capture(), anyMap(), anyLong());
        String uri = uriCaptor.getValue().toString();
        assertTrue(uri.contains("key=g-key"));
        assertTrue(uri.contains("cx=cx-123"));
        assertTrue(uri.contains("q=openai"));
    }

    @Test
    void searchFailsWhenSearchEngineIdMissing() {
        GoogleSearchProvider provider = new GoogleSearchProvider();
        provider.httpClient = mock(ProviderHttpClient.class);

        SearchRequest request = SearchRequest.builder()
            .query("openai")
            .build();
        ProviderConfig config = ProviderConfig.forProvider("google", Map.of("api-key", "g-key"));

        try {
            provider.search(request, config).await().indefinitely();
            fail("Expected failure for missing search-engine-id");
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof ProviderException);
            assertTrue(cause.getMessage().contains("search-engine-id"));
        }
    }
}
