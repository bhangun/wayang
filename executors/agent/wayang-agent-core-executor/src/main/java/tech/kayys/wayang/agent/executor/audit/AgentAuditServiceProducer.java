package tech.kayys.wayang.agent.executor.audit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.core.audit.AgentAuditService;

/**
 * Dispatches the active AgentAuditService implementation based on configuration.
 */
@ApplicationScoped
public class AgentAuditServiceProducer {

    private static final Logger LOG = LoggerFactory.getLogger(AgentAuditServiceProducer.class);

    @ConfigProperty(name = "wayang.agent.audit.strategy", defaultValue = "pgsql")
    String strategy;

    @Inject
    FileAgentAuditService fileService;

    @Inject
    PgsqlAgentAuditService pgsqlService;

    @Produces
    @ApplicationScoped
    public AgentAuditService produceAgentAuditService() {
        LOG.info("Initializing AgentAuditService with strategy: {}", strategy);
        
        return switch (strategy.toLowerCase()) {
            case "file", "local" -> fileService;
            case "pgsql", "database", "db" -> pgsqlService;
            default -> {
                LOG.warn("Unknown audit strategy '{}', defaulting to pgsql", strategy);
                yield pgsqlService;
            }
        };
    }
}
