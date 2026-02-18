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

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.time.Duration;
import java.util.Optional;

/**
 * Configuration for Gamelan Engine connection
 */
@ConfigMapping(prefix = "wayang.orchestration.gamelan")
public interface GamelanEngineConfig {

    @WithDefault("http://localhost:8080")
    String endpoint();

    @WithDefault("wayang-default")
    String tenantId();

    Optional<String> apiKey();

    @WithDefault("30s")
    Duration timeout();
}
