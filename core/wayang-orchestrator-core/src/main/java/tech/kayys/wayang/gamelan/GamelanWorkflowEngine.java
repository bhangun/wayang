/*
 * PolyForm Noncommercial License 1.0.0
 *
 * Copyright (c) 2026 Kayys.tech
 *
 * This software is licensed for non-commercial use only.
 * You may use, modify, and distribute this software for personal,
 * educational, or research purposes.
 *
 * Commercial use, including SaaS or revenue-generating services,
 * requires a separate commercial license from Kayys.tech.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 *
 * @author Bhangun
 */

package tech.kayys.wayang.gamelan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService;
import tech.kayys.gamelan.engine.workflow.WorkflowRunManager;

/**
 * Gamelan-based implementation of Wayang Workflow Engine
 */
@ApplicationScoped
public class GamelanWorkflowEngine extends AbstractGamelanWorkflowEngine {

    protected GamelanWorkflowEngine() {
        super();
    }

    public GamelanWorkflowEngine(GamelanEngineConfig config) {
        this(config, null, null);
    }

    @Inject
    public GamelanWorkflowEngine(
            GamelanEngineConfig config,
            Instance<WorkflowRunManager> runManagerInstance,
            Instance<WorkflowDefinitionService> definitionServiceInstance) {
        super(config);
        if (isLocalRuntimeAvailable(runManagerInstance, definitionServiceInstance)) {
            this.client = GamelanClient.builder()
                    .local(runManagerInstance.get(), definitionServiceInstance.get(), config.tenantId())
                    .timeout(config.timeout())
                    .build();
            return;
        }

        this.client = GamelanClient.builder()
                .restEndpoint(config.endpoint())
                .tenantId(config.tenantId())
                .apiKey(config.apiKey().orElse(null))
                .timeout(config.timeout())
                .build();
    }

    private static boolean isLocalRuntimeAvailable(
            Instance<WorkflowRunManager> runManagerInstance,
            Instance<WorkflowDefinitionService> definitionServiceInstance) {
        return runManagerInstance != null
                && definitionServiceInstance != null
                && runManagerInstance.isResolvable()
                && definitionServiceInstance.isResolvable();
    }
}
