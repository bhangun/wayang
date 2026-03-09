package tech.kayys.wayang.agent.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.kayys.wayang.agent.repository.MessageRepository;
import tech.kayys.wayang.agent.repository.ConfigurationRepository;
import tech.kayys.wayang.agent.model.VectorStore;
import tech.kayys.wayang.agent.repository.InMemoryMessageRepository;
import tech.kayys.wayang.agent.domain.DatabaseMessageRepository;
import tech.kayys.wayang.agent.repository.InMemoryConfigurationRepository;
import tech.kayys.wayang.agent.repository.DatabaseConfigurationRepository;

@ApplicationScoped
public class AgentRepositoryProducer {

    @ConfigProperty(name = "wayang.agent.repository.message.type", defaultValue = "in-memory")
    String messageRepoType;

    @ConfigProperty(name = "wayang.agent.repository.config.type", defaultValue = "in-memory")
    String configRepoType;

    @ConfigProperty(name = "wayang.agent.vectorstore.type", defaultValue = "in-memory")
    String vectorStoreType;

    @Inject
    @jakarta.inject.Named("in-memory")
    InMemoryMessageRepository inMemoryMessageRepository;

    @Inject
    @jakarta.inject.Named("database")
    DatabaseMessageRepository databaseMessageRepository;

    @Inject
    @jakarta.inject.Named("in-memory")
    InMemoryConfigurationRepository inMemoryConfigurationRepository;

    @Inject
    @jakarta.inject.Named("database")
    DatabaseConfigurationRepository databaseConfigurationRepository;

    @Inject
    @jakarta.inject.Named("in-memory")
    InMemoryVectorStore inMemoryVectorStore;

    @Inject
    @jakarta.inject.Named("postgres")
    PostgresVectorStore postgresVectorStore;

    @Inject
    @jakarta.inject.Named("pinecone")
    PineconeVectorStore pineconeVectorStore;

    @Produces
    @ApplicationScoped
    @Default
    public MessageRepository produceMessageRepository() {
        if ("database".equalsIgnoreCase(messageRepoType)) {
            return databaseMessageRepository;
        }
        return inMemoryMessageRepository;
    }

    @Produces
    @ApplicationScoped
    @Default
    public ConfigurationRepository produceConfigurationRepository() {
        if ("database".equalsIgnoreCase(configRepoType)) {
            return databaseConfigurationRepository;
        }
        return inMemoryConfigurationRepository;
    }

    @Produces
    @ApplicationScoped
    @Default
    public VectorStore produceVectorStore() {
        return switch (vectorStoreType.toLowerCase()) {
            case "postgres" -> postgresVectorStore;
            case "pinecone" -> pineconeVectorStore;
            default -> inMemoryVectorStore;
        };
    }
}
