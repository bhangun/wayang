package tech.kayys.wayang.gamelan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService;
import tech.kayys.gamelan.engine.run.WorkflowRunManager;

/**
 * Gamelan-based implementation of Wayang Workflow Engine (Local Transport).
 */
@ApplicationScoped
public class GamelanWorkflowEngine extends AbstractGamelanWorkflowEngine {

    @Inject
    public GamelanWorkflowEngine(GamelanEngineConfig config,
            WorkflowRunManager runManager,
            WorkflowDefinitionService definitionService) {
        super(config);
        this.client = GamelanClient.builder()
                .local(runManager, definitionService, config.tenantId())
                .build();
    }
}
