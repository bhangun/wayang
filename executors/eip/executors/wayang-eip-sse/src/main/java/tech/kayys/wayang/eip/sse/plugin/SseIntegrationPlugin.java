package tech.kayys.wayang.eip.sse.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.plugin.IntegrationDeployment;
import tech.kayys.wayang.eip.plugin.IntegrationPlugin;
import tech.kayys.wayang.eip.plugin.IntegrationPluginContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SseIntegrationPlugin implements IntegrationPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SseIntegrationPlugin.class);

    @Override
    public String id() {
        return "sse";
    }

    @Override
    public String description() {
        return "SSE stream plugin from an SSE endpoint URI into an internal sink.";
    }

    @Override
    public IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception {
        String deploymentId = "sse-" + UUID.randomUUID();
        String routeId = "route-" + deploymentId;

        String endpointUri = stringOption(options, "endpointUri", "ahc-sse:http://localhost:8080/events");
        String sinkUri = stringOption(options, "sinkUri", "seda:eip.sse.out");

        context.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(endpointUri)
                        .routeId(routeId)
                        .to(sinkUri)
                        .log("SSE plugin route " + routeId + " forwarded event to " + sinkUri);
            }
        });

        context.camelContext().getRouteController().startRoute(routeId);
        LOG.info("Deployed SSE route {} from {} to {}", routeId, endpointUri, sinkUri);

        return new IntegrationDeployment(deploymentId, id(), List.of(routeId), options, Instant.now());
    }

    private static String stringOption(Map<String, Object> options, String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
