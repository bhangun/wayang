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
public class FileIntegrationPlugin implements IntegrationPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(FileIntegrationPlugin.class);

    @Override
    public String id() {
        return "file";
    }

    @Override
    public String description() {
        return "File sink route that persists incoming messages to disk.";
    }

    @Override
    public IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception {
        String deploymentId = "file-" + UUID.randomUUID();
        String routeId = "route-" + deploymentId;

        String sourceUri = stringOption(options, "sourceUri", "seda:eip.file.in");
        String directory = stringOption(options, "directory", "./data/out");
        String fileName = stringOption(options, "fileName", "${date:now:yyyyMMddHHmmssSSS}.txt");

        String targetUri = "file:" + directory + "?fileName=" + fileName + "&autoCreate=true";

        context.camelContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(sourceUri)
                        .routeId(routeId)
                        .convertBodyTo(String.class)
                        .to(targetUri)
                        .log("File plugin route " + routeId + " wrote message to " + directory);
            }
        });

        context.camelContext().getRouteController().startRoute(routeId);
        LOG.info("Deployed File integration route {} from {} into {}", routeId, sourceUri, directory);

        return new IntegrationDeployment(deploymentId, id(), List.of(routeId), options, Instant.now());
    }

    private static String stringOption(Map<String, Object> options, String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
