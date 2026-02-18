package tech.kayys.wayang.runtime.standalone;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.control.service.*;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.inference.gollek.GollekInferenceService;
import tech.kayys.wayang.runtime.standalone.status.RuntimeStatusSnapshot;
import tech.kayys.wayang.runtime.standalone.status.RuntimeStatusService;

@QuarkusMain
public class StandaloneRuntime implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(StandaloneRuntime.class);

    @Inject RuntimeStatusService runtimeStatusService;
    @Inject ProjectManager projectManager;
    @Inject WorkflowManager workflowManager;
    @Inject AgentManager agentManager;
    @Inject SchemaRegistryService schemaRegistryService;
    @Inject PluginManagerService pluginManagerService;
    @Inject SchemaValidationService schemaValidationService;
    @Inject GollekInferenceService gollekInferenceService;

    @Override
    public int run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("      WAYANG STANDALONE RUNTIME STARTING         ");
        System.out.println("=================================================");

        RuntimeStatusSnapshot snapshot = runtimeStatusService.collectStatus().await().indefinitely();
        if (snapshot.ready()) {
            LOG.infof("Wayang standalone is ready. Components=%s", snapshot.components());
        } else {
            LOG.warnf("Wayang standalone started with degraded components=%s", snapshot.components());
        }

        // 7. Start Quarkus and wait
        System.out.println("=================================================");
        System.out.println("      WAYANG STANDALONE RUNTIME READY            ");
        System.out.println("=================================================");

        Quarkus.waitForExit();
        return 0;
    }

    @SuppressWarnings("unused")
    private boolean verifySystemReadiness() {
        return projectManager != null
                && workflowManager != null
                && agentManager != null
                && schemaValidationService != null
                && schemaRegistryService != null
                && pluginManagerService != null
                && gollekInferenceService != null;
    }
}
