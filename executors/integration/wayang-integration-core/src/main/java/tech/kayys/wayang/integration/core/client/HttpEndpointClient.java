package tech.kayys.wayang.integration.core.client;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import tech.kayys.wayang.integration.core.config.EndpointConfig;
import tech.kayys.wayang.integration.core.config.AuthConfig;

@ApplicationScoped
public class HttpEndpointClient implements EndpointClient {

    private static final Logger LOG = LoggerFactory.getLogger(HttpEndpointClient.class);

    @Inject
    Vertx vertx;

    private WebClient webClient;
    private ObjectMapper objectMapper;

    @PostConstruct
    void init() {
        WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(5000)
                .setIdleTimeout(30)
                .setMaxPoolSize(100)
                .setKeepAlive(true)
                .setTcpNoDelay(true)
                .setTryUseCompression(true);

        this.webClient = WebClient.create(vertx, options);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Uni<Object> send(EndpointConfig config, Object payload) {
        try {
            URI uri = URI.create(config.uri());
            HttpMethod method = getMethod(config);

            // Create request
            HttpRequest<Buffer> request = webClient
                    .request(method, uri.getPort() != -1 ? uri.getPort() : (uri.getScheme().equals("https") ? 443 : 80),
                            uri.getHost(), uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : ""))
                    .timeout(config.timeoutMs());

            // Add headers
            config.headers().forEach(request::putHeader);

            // Add authentication
            addAuthentication(request, config.auth());

            // Send request
            Uni<HttpResponse<Buffer>> responseUni;
            if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
                responseUni = request.send();
            } else {
                String jsonPayload = payload instanceof String
                        ? (String) payload
                        : objectMapper.writeValueAsString(payload);
                responseUni = request.sendBuffer(Buffer.buffer(jsonPayload));
            }

            return responseUni.map(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("statusCode", resp.statusCode());
                    result.put("statusMessage", resp.statusMessage());

                    Map<String, String> headers = new HashMap<>();
                    resp.headers().names().forEach(name -> headers.put(name, resp.headers().get(name)));
                    result.put("headers", headers);

                    String body = resp.bodyAsString();
                    try {
                        // Try to parse as JSON
                        result.put("body", objectMapper.readValue(body, Object.class));
                    } catch (Exception e) {
                        // Return as string if not JSON
                        result.put("body", body);
                    }
                    return (Object) result;
                } else {
                    LOG.error("HTTP request failed with status: {}", resp.statusCode());
                    throw new RuntimeException("HTTP request failed with status: " + resp.statusCode());
                }
            });

        } catch (Exception e) {
            LOG.error("HTTP request failed", e);
            return Uni.createFrom().failure(e);
        }
    }

    private void addAuthentication(HttpRequest<Buffer> request, AuthConfig auth) {
        if (auth == null || auth.type() == null)
            return;

        switch (auth.type().toLowerCase()) {
            case "basic":
                String basicCreds = Base64.getEncoder()
                        .encodeToString(auth.credential().getBytes(StandardCharsets.UTF_8));
                request.putHeader("Authorization", "Basic " + basicCreds);
                break;
            case "bearer":
                request.putHeader("Authorization", "Bearer " + auth.credential());
                break;
            case "apikey":
                request.putHeader("X-API-Key", auth.credential());
                break;
        }
    }

    private HttpMethod getMethod(EndpointConfig config) {
        String method = (String) config.properties().getOrDefault("method", "POST");
        return switch (method.toUpperCase()) {
            case "GET" -> HttpMethod.GET;
            case "PUT" -> HttpMethod.PUT;
            case "DELETE" -> HttpMethod.DELETE;
            case "PATCH" -> HttpMethod.PATCH;
            default -> HttpMethod.POST;
        };
    }

    @PreDestroy
    void cleanup() {
        if (webClient != null) {
            webClient.close();
        }
    }
}
