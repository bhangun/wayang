package tech.kayys.wayang.eip.plugin.builtin;

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
public class KafkaIntegrationPlugin implements IntegrationPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaIntegrationPlugin.class);

    @Override
    public String id() {
        return "kafka";
    }

    @Override
    public String description() {
        return "Kafka publisher route for event-driven integration.";
    }

    @Override
    public IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception {
        String deploymentId = "kafka-" + UUID.randomUUID();
        String routeId = "route-" + deploymentId;

        String sourceUri = stringOption(options, "sourceUri", "seda:eip.kafka.in");
        String topic = stringOption(options, "topic", "wayang.events");
        String brokers = stringOption(options, "brokers", "localhost:9092");

        String targetUri = "kafka:" + topic + "?brokers=" + brokers;

        context.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(sourceUri)
                        .routeId(routeId)
                        .to(targetUri)
                        .log("Kafka plugin route " + routeId + " published to topic " + topic);
            }
        });

        context.camelContext().getRouteController().startRoute(routeId);
        LOG.info("Deployed Kafka integration route {} from {} to topic {}", routeId, sourceUri, topic);

        return new IntegrationDeployment(deploymentId, id(), List.of(routeId), options, Instant.now());
    }

    private static String stringOption(Map<String, Object> options, String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
