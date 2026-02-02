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
