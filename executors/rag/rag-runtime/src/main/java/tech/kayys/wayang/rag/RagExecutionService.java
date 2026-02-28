package tech.kayys.wayang.rag;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.rag.domain.RagResponse;
import tech.kayys.wayang.rag.domain.RagWorkflowInput;

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
