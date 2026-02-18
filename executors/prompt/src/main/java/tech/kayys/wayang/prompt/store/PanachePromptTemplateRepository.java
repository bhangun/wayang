package tech.kayys.wayang.prompt.store;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import tech.kayys.wayang.prompt.core.PromptTemplate;

import java.util.List;

/**
 * ============================================================================
 * PanachePromptTemplateRepository — Hibernate Reactive Panache implementation.
 * ============================================================================
 *
 * This is the default repository bean active in the {@code prod} and
 * {@code staging} profiles. It uses Hibernate Reactive + Panache for fully
 * non-blocking PostgreSQL access.
 *
 * Tenant isolation
 * ----------------
 * Every query includes {@code WHERE tenant_id = :tenantId}. The
 * {@link #save} method uses merge semantics (upsert) so that re-saving an
 * updated template overwrites the existing row.
 *
 * Flyway migration
 * ----------------
 * The {@code prompt_templates} table is created by a Flyway migration script
 * (not shown here). The entity annotations above serve as the authoritative
 * schema reference.
 */
@ApplicationScoped
public class PanachePromptTemplateRepository implements PromptTemplateRepository {

    private static final Logger LOG = Logger.getLogger(PanachePromptTemplateRepository.class);

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    @Transactional
    public Uni<PromptTemplate> save(PromptTemplate template) {
        PromptTemplateEntity entity = PromptTemplateEntity.fromDomain(template);

        return sessionFactory.withTransaction(session -> session.createQuery(
                "FROM PromptTemplateEntity e WHERE e.templateId = :tid AND e.tenantId = :tenant",
                PromptTemplateEntity.class)
                .setParameter("tid", template.getTemplateId())
                .setParameter("tenant", template.getTenantId())
                .getSingleResultOrNull()
                .onItem().transformToUni(existing -> {
                    if (existing != null) {
                        // Update existing
                        existing.setName(entity.getName());
                        existing.setStatus(entity.getStatus());
                        existing.setActiveVersion(entity.getActiveVersion());
                        existing.setDefinition(entity.getDefinition());
                        existing.setUpdatedAt(entity.getUpdatedAt());
                        return session.flush().replaceWith(existing);
                    } else {
                        // Insert new
                        return session.persist(entity).replaceWith(entity);
                    }
                })
                .map(PromptTemplateEntity::toDomain));
    }

    @Override
    public Uni<PromptTemplate> findById(String templateId, String tenantId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "FROM PromptTemplateEntity e WHERE e.templateId = :tid AND e.tenantId = :tenant",
                PromptTemplateEntity.class)
                .setParameter("tid", templateId)
                .setParameter("tenant", tenantId)
                .getSingleResultOrNull()
                .map(entity -> entity != null ? entity.toDomain() : null));
    }

    @Override
    public Uni<List<PromptTemplate>> findByTenant(String tenantId, int page, int pageSize) {
        return sessionFactory.withSession(session -> session.createQuery(
                "FROM PromptTemplateEntity e WHERE e.tenantId = :tenant ORDER BY e.createdAt DESC",
                PromptTemplateEntity.class)
                .setParameter("tenant", tenantId)
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList()
                .map(entities -> entities.stream()
                        .map(PromptTemplateEntity::toDomain)
                        .toList()));
    }

    @Override
    @Transactional
    public Uni<Void> delete(String templateId, String tenantId) {
        return sessionFactory.withTransaction(session -> session.createQuery(
                "DELETE FROM PromptTemplateEntity e WHERE e.templateId = :tid AND e.tenantId = :tenant")
                .setParameter("tid", templateId)
                .setParameter("tenant", tenantId)
                .executeUpdate()
                .replaceWithVoid());
    }
}
