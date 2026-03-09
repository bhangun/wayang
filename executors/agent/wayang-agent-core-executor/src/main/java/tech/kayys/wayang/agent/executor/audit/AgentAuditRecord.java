package tech.kayys.wayang.agent.executor.audit;

import java.time.Instant;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent_audit_records")
public class AgentAuditRecord extends PanacheEntityBase {

    @Id
    public String id;

    @Column(name = "run_id", nullable = false)
    public String runId;

    @Column(name = "tenant_id", nullable = false)
    public String tenantId;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "content", columnDefinition = "TEXT")
    public String content;

    @Column(name = "format")
    public String format;

    @Column(name = "created_at")
    public Instant createdAt;
}
