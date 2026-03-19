package tech.kayys.wayang.agent.executor;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.mockito.Mockito;

import tech.kayys.gamelan.engine.repository.WorkflowDefinitionRepository;
import tech.kayys.wayang.agent.skill.SkillRegistry;
import tech.kayys.wayang.agent.skill.SkillPromptRenderer;
import tech.kayys.wayang.agent.core.tool.ToolRegistry;
import tech.kayys.wayang.agent.core.memory.AgentMemoryManager;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.memory.spi.AgentMemory;
import tech.kayys.wayang.guardrails.GuardrailsService;
import tech.kayys.wayang.tool.spi.ToolExecutor;
import tech.kayys.wayang.prompt.core.PromptEngine;
import tech.kayys.wayang.prompt.core.RenderingEngineRegistry;
import tech.kayys.wayang.prompt.registry.PromptTemplateRegistry;
import tech.kayys.gollek.sdk.core.GollekSdk;

@ApplicationScoped
public class MockProducers {

    @Produces
    @ApplicationScoped
    @Mock
    public SkillRegistry mockSkillRegistry() {
        return Mockito.mock(SkillRegistry.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public ToolRegistry mockToolRegistry() {
        return Mockito.mock(ToolRegistry.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public AgentMemory mockAgentMemory() {
        return Mockito.mock(AgentMemory.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public GuardrailsService mockGuardrailsService() {
        return Mockito.mock(GuardrailsService.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public ToolExecutor mockToolExecutor() {
        return Mockito.mock(ToolExecutor.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public WorkflowDefinitionRepository mockWorkflowDefinitionRepository() {
        return Mockito.mock(WorkflowDefinitionRepository.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public PromptEngine mockPromptEngine() {
        return Mockito.mock(PromptEngine.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public RenderingEngineRegistry mockRenderingEngineRegistry() {
        return Mockito.mock(RenderingEngineRegistry.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public PromptTemplateRegistry mockPromptTemplateRegistry() {
        return Mockito.mock(PromptTemplateRegistry.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public GollekInferenceService mockGollekInferenceService() {
        return Mockito.mock(GollekInferenceService.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public GollekSdk mockGollekSdk() {
        return Mockito.mock(GollekSdk.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public AgentMemoryManager mockAgentMemoryManager() {
        return Mockito.mock(AgentMemoryManager.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public SkillPromptRenderer mockSkillPromptRenderer() {
        return Mockito.mock(SkillPromptRenderer.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public io.vertx.mutiny.ext.web.client.WebClient mockWebClient() {
        return Mockito.mock(io.vertx.mutiny.ext.web.client.WebClient.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public tech.kayys.wayang.embedding.EmbeddingService mockEmbeddingService() {
        return Mockito.mock(tech.kayys.wayang.embedding.EmbeddingService.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public tech.kayys.wayang.memory.service.VectorMemoryStore mockVectorMemoryStore() {
        return Mockito.mock(tech.kayys.wayang.memory.service.VectorMemoryStore.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService mockWorkflowDefinitionService() {
        return Mockito.mock(tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public tech.kayys.gamelan.engine.workflow.WorkflowRunManager mockWorkflowRunManager() {
        return Mockito.mock(tech.kayys.gamelan.engine.workflow.WorkflowRunManager.class);
    }

    @Produces
    @ApplicationScoped
    @Mock
    public tech.kayys.gamelan.engine.repository.WorkflowRunRepository mockWorkflowRunRepository() {
        return Mockito.mock(tech.kayys.gamelan.engine.repository.WorkflowRunRepository.class);
    }
}
