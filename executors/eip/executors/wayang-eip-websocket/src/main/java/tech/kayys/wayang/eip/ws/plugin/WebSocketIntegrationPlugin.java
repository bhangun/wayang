package tech.kayys.wayang.eip.ws.plugin;

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
public class WebSocketIntegrationPlugin implements IntegrationPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketIntegrationPlugin.class);

    @Override
    public String id() {
        return "websocket";
    }

    @Override
    public String description() {
        return "WebSocket bridge plugin using Camel endpoint URIs.";
    }

    @Override
    public IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception {
        String deploymentId = "websocket-" + UUID.randomUUID();
        String routeId = "route-" + deploymentId;

        String sourceUri = stringOption(options, "sourceUri", "seda:eip.websocket.in");
        String endpointUri = stringOption(options, "endpointUri", "websocket://0.0.0.0:9091/ws?sendToAll=true");

        context.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(sourceUri)
                        .routeId(routeId)
                        .convertBodyTo(String.class)
                        .toD(endpointUri)
                        .log("WebSocket plugin route " + routeId + " sent message to " + endpointUri);
            }
        });

        context.camelContext().getRouteController().startRoute(routeId);
        LOG.info("Deployed WebSocket route {} from {} to {}", routeId, sourceUri, endpointUri);

        return new IntegrationDeployment(deploymentId, id(), List.of(routeId), options, Instant.now());
    }

    private static String stringOption(Map<String, Object> options, String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
