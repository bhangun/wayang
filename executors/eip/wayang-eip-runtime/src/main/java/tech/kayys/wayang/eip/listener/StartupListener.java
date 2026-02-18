package tech.kayys.wayang.eip.listener;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.service.IntegrationService;

@ApplicationScoped
public class StartupListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartupListener.class);

    @Inject
    IntegrationService integrationService;

    void onStart(@Observes StartupEvent event) {
        LOG.info("Starting Wayang EIP runtime with plugin-based integration modules");
        integrationService.deployConfiguredModules();
        LOG.info("Wayang EIP runtime started with {} active deployments", integrationService.listDeployments().size());
    }
}
