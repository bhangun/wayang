package tech.kayys.wayang.eip.plugin.builtin;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.plugin.IntegrationDeployment;
import tech.kayys.wayang.eip.plugin.IntegrationPlugin;
import tech.kayys.wayang.eip.plugin.IntegrationPluginContext;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class HttpIntegrationPlugin implements IntegrationPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(HttpIntegrationPlugin.class);

    @Override
    public String id() {
        return "http";
    }

    @Override
    public String description() {
        return "HTTP bridge from an internal endpoint to external REST APIs.";
    }

    @Override
    public IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception {
        String deploymentId = "http-" + UUID.randomUUID();
        String routeId = "route-" + deploymentId;

        String sourceUri = stringOption(options, "sourceUri", "seda:eip.http.in");
        String method = stringOption(options, "method", "GET");
        String targetUrl = requiredOption(options, "targetUrl");

        context.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(sourceUri)
                        .routeId(routeId)
                        .setHeader(Exchange.HTTP_METHOD, constant(method))
                        .toD(targetUrl)
                        .log("HTTP plugin route " + routeId + " returned ${header.CamelHttpResponseCode}");
            }
        });

        context.camelContext().getRouteController().startRoute(routeId);
        LOG.info("Deployed HTTP integration route {} from {} to {}", routeId, sourceUri, targetUrl);

        return new IntegrationDeployment(deploymentId, id(), List.of(routeId), options, Instant.now());
    }

    private static String requiredOption(Map<String, Object> options, String key) {
        String value = stringOption(options, key, "");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Missing required option: " + key);
        }
        return value;
    }

    private static String stringOption(Map<String, Object> options, String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
