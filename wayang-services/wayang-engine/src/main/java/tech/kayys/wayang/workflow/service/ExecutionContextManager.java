package tech.kayys.wayang.workflow.service;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.node.model.ExecutionContext;
import java.util.UUID;
import java.time.Instant;

@ApplicationScoped
public class ExecutionContextManager {
    public ExecutionContext createContext() {
        return ExecutionContext.builder()
                .runId(UUID.randomUUID().toString())
                .startTime(Instant.now())
                .build();
    }

    public ExecutionContext getContext(String id) {
        return ExecutionContext.builder()
                .runId(id)
                .startTime(Instant.now())
                .build();
    }
}
