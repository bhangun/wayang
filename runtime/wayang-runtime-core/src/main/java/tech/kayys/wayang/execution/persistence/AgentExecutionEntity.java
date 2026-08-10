package tech.kayys.wayang.execution.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "agent_execution")
public class AgentExecutionEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    public String id;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "agent_context", columnDefinition = "jsonb")
    public String agentContextJson;

    public AgentExecutionEntity() {
    }

    public static AgentExecutionEntity findById(String id) {
        return find("id", id).firstResult();
    }
}
