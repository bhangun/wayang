package tech.kayys.wayang.engine.gamelan;

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
