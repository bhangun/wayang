package tech.kayys.wayang.engine.gamelan;

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

    private final GamelanWorkflowEngine engine;

    @Inject
    public GamelanService(GamelanWorkflowEngine engine) {
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