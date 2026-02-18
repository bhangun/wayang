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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage Gamelan orchestration operations
 */
@ApplicationScoped
public class GamelanService {

    private static final Logger LOG = LoggerFactory.getLogger(GamelanService.class);

    private final AbstractGamelanWorkflowEngine engine;

    @Inject
    public GamelanService(AbstractGamelanWorkflowEngine engine) {
        this.engine = engine;
    }

    public void testConnection() {
        LOG.info("Testing connection to Gamelan...");
        engine.listWorkflows().subscribe().with(
                workflows -> {
                    LOG.info("Connected to Gamelan. Found {} workflows.", workflows.size());
                    workflows.forEach(w -> LOG.info("- {}", w.name()));
                },
                failure -> LOG.error("Failed to connect to Gamelan: {}", failure.getMessage()));
    }
}