package tech.kayys.wayang.memory.scheduler;

import tech.kayys.wayang.memory.service.MemoryOptimizationService;
import tech.kayys.wayang.memory.service.MemorySecurityService;
import tech.kayys.wayang.memory.service.MemoryService;
import tech.kayys.wayang.memory.entity.MemorySessionEntity;
import tech.kayys.wayang.memory.model.MemoryContext;
import tech.kayys.wayang.memory.model.SecurityScanResult;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@ApplicationScoped
public class MemoryMaintenanceScheduler {
    
    private static final Logger LOG = LoggerFactory.getLogger(MemoryMaintenanceScheduler.class);
    
    @Inject
    MemoryOptimizationService optimizationService;
    
    @Inject
    MemorySecurityService securityService;
    
    @Inject
    MemoryService memoryService;

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    @WithSession
    public Uni<Void> optimizeStaleMemories() {
        LOG.info("Starting scheduled memory optimization");
        
        return MemorySessionEntity.<MemorySessionEntity>findAll().list()
            .onItem().transformToMulti(sessions -> Multi.createFrom().iterable(sessions))
            .onItem().transformToUniAndMerge(session -> 
                optimizationService.optimizeMemory(session.sessionId)
                    .onItem().invoke(result -> 
                        LOG.info("Optimized session: {}, saved: {} bytes", 
                            session.sessionId, result.getSpaceSaved()))
                    .onFailure().invoke(throwable -> 
                        LOG.error("Failed to optimize session: {}", session.sessionId, throwable)))
            .collect().asList()
            .onItem().invoke(results -> LOG.info("Completed optimization for {} sessions", results.size()))
            .replaceWithVoid();
    }

    @Scheduled(cron = "0 0 3 * * ?") // Run at 3 AM daily
    @WithSession
    public Uni<Void> cleanupExpiredSessions() {
        LOG.info("Starting cleanup of expired sessions");
        
        Instant now = Instant.now();
        
        return MemorySessionEntity.<MemorySessionEntity>delete(
                "expiresAt < ?1 AND expiresAt IS NOT NULL", now)
            .onItem().invoke(count -> LOG.info("Deleted {} expired sessions", count))
            .replaceWithVoid();
    }

    @Scheduled(every = "6h") // Run every 6 hours
    @WithSession
    public Uni<Void> auditMemorySecurity() {
        LOG.info("Starting security audit of memory");
        
        return MemorySessionEntity.<MemorySessionEntity>findAll().list()
            .onItem().transformToMulti(sessions -> Multi.createFrom().iterable(sessions))
            .select().first(100) // Limit to 100 sessions per run
            .onItem().transformToUniAndMerge(session -> 
                memoryService.getContext(session.sessionId, session.userId)
                    .onItem().transformToUni(context -> 
                        securityService.scanMemoryForPII(context))
                    .onItem().invoke(scanResult -> {
                        if (!scanResult.isPassed()) {
                            LOG.warn("Security violations found in session: {}, violations: {}", 
                                session.sessionId, scanResult.getViolations().size());
                        }
                    })
                    .onFailure().invoke(throwable -> 
                        LOG.error("Failed to scan session: {}", session.sessionId, throwable)))
            .collect().asList()
            .onItem().invoke(results -> LOG.info("Completed security audit for {} sessions", results.size()))
            .replaceWithVoid();
    }
}