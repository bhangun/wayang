package tech.kayys.gamelan.executor.rag.langchain;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.executor.rag.domain.RagResponse;
import tech.kayys.gamelan.executor.rag.domain.RagWorkflowInput;
import tech.kayys.gamelan.executor.rag.examples.RagQueryService;

@ApplicationScoped
public class RagExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(RagExecutionService.class);

    @Inject
    RagQueryService ragQueryService;

    public Uni<RagResponse> executeRagWorkflow(RagWorkflowInput input) {
        LOG.info("Executing native RAG workflow for tenant: {}", input.tenantId());
        return ragQueryService.query(input.tenantId(), input.query(), "default");
    }
}
