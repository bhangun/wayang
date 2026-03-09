package tech.kayys.wayang.agent.service;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.mockito.Mockito;
import tech.kayys.gamelan.engine.repository.WorkflowDefinitionRepository;
import tech.kayys.wayang.agent.domain.DatabaseMessageRepository;
import tech.kayys.wayang.agent.model.VectorStore;
import tech.kayys.wayang.agent.repository.DatabaseConfigurationRepository;
import tech.kayys.wayang.agent.repository.InMemoryConfigurationRepository;
import tech.kayys.wayang.agent.repository.InMemoryMessageRepository;
import tech.kayys.wayang.prompt.core.PromptEngine;
import tech.kayys.wayang.prompt.core.RenderingEngineRegistry;
import tech.kayys.wayang.prompt.registry.PromptTemplateRegistry;

@Mock
@ApplicationScoped
public class MockProducers {

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("database")
    public DatabaseMessageRepository mockDatabaseMessageRepository() {
        return Mockito.mock(DatabaseMessageRepository.class);
    }

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("in-memory")
    public InMemoryMessageRepository mockInMemoryMessageRepository() {
        return Mockito.mock(InMemoryMessageRepository.class);
    }

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("database")
    public DatabaseConfigurationRepository mockDatabaseConfigurationRepository() {
        return Mockito.mock(DatabaseConfigurationRepository.class);
    }

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("in-memory")
    public InMemoryConfigurationRepository mockInMemoryConfigurationRepository() {
        return Mockito.mock(InMemoryConfigurationRepository.class);
    }

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("postgres")
    public PostgresVectorStore mockPostgresVectorStore() {
        return Mockito.mock(PostgresVectorStore.class);
    }

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("pinecone")
    public PineconeVectorStore mockPineconeVectorStore() {
        return Mockito.mock(PineconeVectorStore.class);
    }

    @Produces
    @ApplicationScoped
    @jakarta.inject.Named("in-memory")
    public InMemoryVectorStore mockInMemoryVectorStore() {
        return Mockito.mock(InMemoryVectorStore.class);
    }

    @Produces
    @ApplicationScoped
    public PromptEngine mockPromptEngine() {
        return Mockito.mock(PromptEngine.class);
    }

    @Produces
    @ApplicationScoped
    public RenderingEngineRegistry mockRenderingEngineRegistry() {
        return Mockito.mock(RenderingEngineRegistry.class);
    }

    @Produces
    @ApplicationScoped
    public PromptTemplateRegistry mockPromptTemplateRegistry() {
        return Mockito.mock(PromptTemplateRegistry.class);
    }

    @Produces
    @ApplicationScoped
    public WorkflowDefinitionRepository mockWorkflowDefinitionRepository() {
        return Mockito.mock(WorkflowDefinitionRepository.class);
    }
}
