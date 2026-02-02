package io.wayang.executors.integration.runtime.listener;

import io.wayang.executors.integration.runtime.config.IntegrationConfig;
import io.wayang.executors.integration.runtime.service.IntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class StartupListener {

    private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);

    @Inject
    IntegrationConfig integrationConfig;

    @Inject
    IntegrationService integrationService;

    public void onStart(@Observes StartupEvent ev) {
        logger.info("Starting Wayang Integration Runtime...");
        
        // Initialize enabled modules
        String enabledModules = integrationConfig.enabledModules().orElse("all");
        logger.info("Enabled modules: {}", enabledModules);
        
        if (!"none".equals(enabledModules)) {
            String[] modules = enabledModules.split(",");
            for (String module : modules) {
                module = module.trim();
                integrationService.initializeIntegration(module);
            }
        }
        
        logger.info("Wayang Integration Runtime started successfully.");
    }
}