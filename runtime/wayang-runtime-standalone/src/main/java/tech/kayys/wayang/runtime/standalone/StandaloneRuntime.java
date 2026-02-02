package tech.kayys.wayang.runtime.standalone;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.service.WorkflowManager;
import tech.kayys.wayang.engine.gamelan.GamelanService;
import tech.kayys.wayang.plugin.ControlPlanePluginManager;

@QuarkusMain
public class StandaloneRuntime implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(StandaloneRuntime.class);

    @Inject
    GamelanService gamelanService;

    @Inject
    ControlPlanePluginManager pluginManager;

    @Inject
    ProjectManager projectManager;

    @Inject
    WorkflowManager workflowManager;

    @Override
    public int run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("      WAYANG STANDALONE RUNTIME STARTING         ");
        System.out.println("=================================================");

        // 1. Initialize Plugins
        LOG.info("Initializing Plugin Manager...");
        int plugins = pluginManager.getRegisteredPlugins().size();
        LOG.infof("Plugin Manager initialized. Loaded %d plugins.", plugins);

        // 2. Test Orchestration Connection
        LOG.info("Verifying Gamelan Orchestration Engine...");
        gamelanService.testConnection();

        // 3. Verify Control Plane Services
        LOG.info("Verifying Control Plane Services...");
        if (projectManager != null && workflowManager != null) {
            LOG.info("Control Plane Services (ProjectManager, WorkflowManager) are active.");
        } else {
            LOG.error("Control Plane Services failed to inject.");
        }

        // 4. Start Quarkus and wait
        System.out.println("=================================================");
        System.out.println("      WAYANG STANDALONE RUNTIME READY            ");
        System.out.println("=================================================");
        
        Quarkus.waitForExit();
        return 0;
    }
}
