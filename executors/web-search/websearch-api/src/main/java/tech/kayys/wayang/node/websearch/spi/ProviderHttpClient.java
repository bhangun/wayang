package tech.kayys.wayang.node.websearch.spi;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;

import java.net.URI;
import java.util.Map;

public interface ProviderHttpClient {
    Uni<JsonNode> getJson(String providerId, URI uri, Map<String, String> headers, long timeoutMs);
}
