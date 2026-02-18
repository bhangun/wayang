package tech.kayys.wayang.eip.grpc.plugin;

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
public class GrpcIntegrationPlugin implements IntegrationPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcIntegrationPlugin.class);

    @Override
    public String id() {
        return "grpc";
    }

    @Override
    public String description() {
        return "gRPC bridge plugin using Camel endpoint URIs.";
    }

    @Override
    public IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception {
        String deploymentId = "grpc-" + UUID.randomUUID();
        String routeId = "route-" + deploymentId;

        String sourceUri = stringOption(options, "sourceUri", "seda:eip.grpc.in");
        String endpointUri = stringOption(options, "endpointUri",
                "grpc://localhost:9000/demo.EchoService/echo?method=echo&synchronous=true&usePlainText=true");

        context.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(sourceUri)
                        .routeId(routeId)
                        .toD(endpointUri)
                        .log("gRPC plugin route " + routeId + " sent message to " + endpointUri);
            }
        });

        context.camelContext().getRouteController().startRoute(routeId);
        LOG.info("Deployed gRPC route {} from {} to {}", routeId, sourceUri, endpointUri);

        return new IntegrationDeployment(deploymentId, id(), List.of(routeId), options, Instant.now());
    }

    private static String stringOption(Map<String, Object> options, String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
