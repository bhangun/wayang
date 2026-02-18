package tech.kayys.wayang.node.websearch.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.node.websearch.exception.ProviderException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@ApplicationScoped
public class JdkProviderHttpClient implements ProviderHttpClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Uni<JsonNode> getJson(String providerId, URI uri, Map<String, String> headers, long timeoutMs) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .timeout(Duration.ofMillis(Math.max(200L, timeoutMs)));

        headers.forEach(requestBuilder::header);
        HttpRequest request = requestBuilder.build();

        return Uni.createFrom()
            .completionStage(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
            .onItem().transform(response -> parseResponse(providerId, uri, response))
            .onFailure().transform(throwable ->
                throwable instanceof ProviderException
                    ? throwable
                    : new ProviderException(providerId, "HTTP request failed: " + throwable.getMessage())
            );
    }

    private JsonNode parseResponse(String providerId, URI uri, HttpResponse<String> response) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            Long retryAfterMillis = parseRetryAfterMillis(response);
            throw new ProviderException(providerId, status, "HTTP error for " + uri, retryAfterMillis);
        }
        try {
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new ProviderException(providerId, "Invalid JSON response: " + e.getMessage());
        }
    }

    private Long parseRetryAfterMillis(HttpResponse<String> response) {
        return response.headers()
            .firstValue("Retry-After")
            .map(String::trim)
            .flatMap(value -> {
                try {
                    long seconds = Long.parseLong(value);
                    return seconds > 0 ? java.util.Optional.of(seconds * 1000L) : java.util.Optional.<Long>empty();
                } catch (NumberFormatException ignored) {
                    return java.util.Optional.empty();
                }
            })
            .orElse(null);
    }
}
