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
import jakarta.inject.Inject;
import tech.kayys.gamelan.sdk.client.GamelanClient;

/**
 * Gamelan-based implementation of Wayang Workflow Engine (Remote Transport).
 */
@ApplicationScoped
public class GamelanWorkflowEngine extends AbstractGamelanWorkflowEngine {

    @Inject
    public GamelanWorkflowEngine(GamelanEngineConfig config) {
        super(config);
        this.client = GamelanClient.builder()
                .restEndpoint(config.endpoint())
                .tenantId(config.tenantId())
                .apiKey(config.apiKey().orElse(null))
                .timeout(config.timeout())
                .build();
    }
}
