package tech.kayys.wayang.runtime.standalone.repository;

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tech.kayys.gamelan.engine.repository.WorkflowDefinitionRepository;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionId;

@ApplicationScoped
@IfBuildProperty(name = "quarkus.datasource.db-kind", stringValue = "h2")
public class InMemoryWorkflowDefinitionRepository implements WorkflowDefinitionRepository {

    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();

    @Override
    public Uni<WorkflowDefinition> findById(WorkflowDefinitionId id, TenantId tenantId) {
        return Uni.createFrom().item(definitions.get(key(id, tenantId)));
    }

    @Override
    public Uni<WorkflowDefinition> save(WorkflowDefinition definition, TenantId tenantId) {
        definitions.put(key(definition.id(), tenantId), definition);
        return Uni.createFrom().item(definition);
    }

    @Override
    public Uni<List<WorkflowDefinition>> findByTenant(TenantId tenantId, boolean activeOnly) {
        String prefix = tenantId.value() + ":";
        return Uni.createFrom().item(definitions.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .toList());
    }

    @Override
    public Uni<WorkflowDefinition> findByName(String name, TenantId tenantId) {
        String prefix = tenantId.value() + ":";
        return Uni.createFrom().item(definitions.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .filter(def -> def.name().equals(name))
                .findFirst()
                .orElse(null));
    }

    @Override
    public Uni<Void> delete(WorkflowDefinitionId id, TenantId tenantId) {
        definitions.remove(key(id, tenantId));
        return Uni.createFrom().voidItem();
    }

    private String key(WorkflowDefinitionId id, TenantId tenantId) {
        return tenantId.value() + ":" + id.value();
    }
}
