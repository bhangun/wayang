package tech.kayys.wayang.guardrails.policy;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.guardrails.plugin.api.CheckPhase;

import java.util.List;

@ApplicationScoped
public class InMemoryPolicyRepository implements PolicyRepository {
    @Override
    public Uni<List<Policy>> findActivePolices(String tenantId, CheckPhase phase) {
        return Uni.createFrom().item(List.of());
    }
}
