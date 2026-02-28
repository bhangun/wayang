package tech.kayys.wayang.integration.core;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.sdk.executor.RemoteExecutorRuntime;

/**
 * Runtime for Wayang Integration Core executors.
 * Extends RemoteExecutorRuntime to gain auto-registration and heartbeat
 * capabilities.
 */
@Startup
@ApplicationScoped
public class WayangIntegrationRuntime extends RemoteExecutorRuntime {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WayangIntegrationRuntime.class);

    @Override
    public void start() {
        LOG.info("Starting Wayang Integration Core Runtime...");
        super.start();
        LOG.info("Wayang Integration Runtime active with {} executors.", executors.size());
        executors.forEach((type, exec) -> LOG.debug("Loaded Wayang Executor: {}", type));
    }
}
