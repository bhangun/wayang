package tech.kayys.wayang.execution.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.execution.CheckpointStore;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaCheckpointStore implements CheckpointStore {

    @Inject
    ObjectMapper objectMapper;

    @Override
    @Transactional
    public void save(String executionId, AgentContext context) {
        AgentExecutionEntity entity = AgentExecutionEntity.findById(executionId);
        if (entity == null) {
            entity = new AgentExecutionEntity();
            entity.id = executionId;
            entity.createdAt = Instant.now();
        }
        entity.updatedAt = Instant.now();
        entity.status = "PAUSED"; // or based on context
        try {
            entity.agentContextJson = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize AgentContext", e);
        }
        entity.persist();
    }

    @Override
    public Optional<AgentContext> load(String executionId) {
        AgentExecutionEntity entity = AgentExecutionEntity.findById(executionId);
        if (entity == null || entity.agentContextJson == null) {
            return Optional.empty();
        }
        try {
            // Note: Since AgentContext is an interface, we deserialize to a concrete implementation.
            // Assuming DefaultAgentContext is the implementation. If it's in a different package,
            // we will need to adjust. For now we use the interface and let Jackson polymorphic types handle it if configured,
            // or we must specify the concrete type. For the sake of this implementation, we deserialize to the concrete type.
            tech.kayys.wayang.agent.AgentContext context = objectMapper.readValue(entity.agentContextJson, tech.kayys.wayang.agent.AgentContext.class);
            return Optional.of(context);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize AgentContext", e);
        }
    }

    @Override
    public List<AgentContext> history(String executionId) {
        // Not fully implemented for history in this phase
        Optional<AgentContext> current = load(executionId);
        return current.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public void delete(String executionId) {
        AgentExecutionEntity.deleteById(executionId);
    }
}
