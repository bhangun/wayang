package tech.kayys.wayang.guardrails.policy;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.guardrails.detector.CheckPhase;
import java.util.List;

public interface PolicyRepository {
    Uni<List<Policy>> findActivePolices(String tenantId, CheckPhase phase);
}
