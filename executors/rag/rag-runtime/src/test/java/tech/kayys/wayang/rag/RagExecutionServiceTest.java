package tech.kayys.gamelan.executor.rag.langchain;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gamelan.executor.rag.domain.GenerationConfig;
import tech.kayys.gamelan.executor.rag.domain.RagResponse;
import tech.kayys.gamelan.executor.rag.domain.RagWorkflowInput;
import tech.kayys.gamelan.executor.rag.domain.RetrievalConfig;
import tech.kayys.gamelan.executor.rag.examples.RagQueryService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagExecutionServiceTest {

    @Mock
    private RagQueryService ragQueryService;

    private RagExecutionService ragExecutionService;

    @BeforeEach
    void setUp() {
        ragExecutionService = new RagExecutionService();
        ragExecutionService.ragQueryService = ragQueryService;
    }

    @Test
    void testExecuteRagWorkflow_Success() {
        when(ragQueryService.query(anyString(), anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(new RagResponse(
                        "q", "a", List.of(), List.of(), null, null, Instant.now(), Map.of(), List.of(), Optional.empty())));

        RagResponse response = ragExecutionService.executeRagWorkflow(
                new RagWorkflowInput("tenant", "q", RetrievalConfig.defaults(), GenerationConfig.defaults()))
                .await().indefinitely();

        assertEquals("a", response.answer());
    }
}
