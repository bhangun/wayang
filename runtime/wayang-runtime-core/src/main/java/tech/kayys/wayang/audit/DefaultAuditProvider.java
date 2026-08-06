package tech.kayys.wayang.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultAuditProvider implements AuditProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();
    
    public DefaultAuditProvider() {
        this.id = Id.random().asString();
        this.name = "default-audit-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Audit Provider")
            .version(version)
            .label("type", "audit")
            .now()
            .build();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("audit"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public void record(AuditEvent event) throws Exception {
        events.add(event);
        System.out.println("Audit: " + event.action() + " by " + 
            (event.principal() != null ? event.principal().username() : "unknown"));
    }
    
    @Override
    public List<AuditEvent> query(AuditQuery query) throws Exception {
        return events.stream()
            .filter(e -> {
                if (query.userId() != null && !e.principal().id().asString().equals(query.userId())) {
                    return false;
                }
                if (query.action() != null && !e.action().equals(query.action())) {
                    return false;
                }
                if (query.targetType() != null && !query.targetType().equals(e.targetType())) {
                    return false;
                }
                if (query.targetId() != null && !query.targetId().equals(e.targetId())) {
                    return false;
                }
                if (query.from() != null && e.timestamp().isBefore(query.from())) {
                    return false;
                }
                if (query.to() != null && e.timestamp().isAfter(query.to())) {
                    return false;
                }
                return true;
            })
            .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
            .limit(query.limit())
            .toList();
    }
    
    public List<AuditEvent> getEvents() {
        return new ArrayList<>(events);
    }
}