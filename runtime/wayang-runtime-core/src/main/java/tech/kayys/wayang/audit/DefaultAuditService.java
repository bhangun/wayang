package tech.kayys.wayang.audit;

import java.io.FileWriter;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.database.DatabaseService;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Default Audit Service Implementation
 */
public class DefaultAuditService implements AuditService {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final DatabaseService db;
    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();
    private final Map<String, AuditStats> statsCache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean initialized = false;
    
    public DefaultAuditService(DatabaseService db) {
        this.id = Id.random().asString();
        this.name = "audit-service";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Wayang Audit Service")
            .version(version)
            .label("type", "audit")
            .now()
            .build();
        this.db = db;
        
        // Initialize database table
        createAuditTable();
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
    public void log(AuditEvent event) throws Exception {
        events.add(event);
        
        // Async insert into database
        executor.submit(() -> {
            try {
                saveToDatabase(event);
                // Invalidate cache
                statsCache.clear();
            } catch (Exception e) {
                // Log error but don't fail
                System.err.println("Failed to save audit event: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void log(String action, Principal principal, Object target) throws Exception {
        log(action, principal, target, AuditResult.SUCCESS, null);
    }
    
    @Override
    public void log(String action, Principal principal, Object target, String result) throws Exception {
        log(action, principal, target, result, null);
    }
    
    @Override
    public void log(String action, Principal principal, Object target, String result, String details) throws Exception {
        AuditEvent event = AuditEvent.builder()
            .action(action)
            .principal(principal)
            .targetType(target != null ? target.getClass().getSimpleName() : null)
            .targetId(target != null ? target.toString() : null)
            .result("success".equalsIgnoreCase(result) ? AuditResult.SUCCESS : AuditResult.FAILURE)
            .details(details)
            .build();
        log(event);
    }
    
    @Override
    public void log(String action, Principal principal, Object target, AuditResult result, String details) throws Exception {
        AuditEvent event = AuditEvent.builder()
            .action(action)
            .principal(principal)
            .targetType(target != null ? target.getClass().getSimpleName() : null)
            .targetId(target != null ? target.toString() : null)
            .result(result)
            .details(details)
            .build();
        log(event);
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
                if (query.result() != null && e.result() != query.result()) {
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
            .sorted((a, b) -> {
                int cmp = a.timestamp().compareTo(b.timestamp());
                return query.ascending() ? cmp : -cmp;
            })
            .skip(query.offset())
            .limit(query.limit())
            .toList();
    }
    
    @Override
    public List<AuditEvent> getByUser(String userId) throws Exception {
        return query(AuditQuery.builder().userId(userId).build());
    }
    
    @Override
    public List<AuditEvent> getByAction(String action) throws Exception {
        return query(AuditQuery.builder().action(action).build());
    }
    
    @Override
    public List<AuditEvent> getByTarget(String targetType, String targetId) throws Exception {
        return query(AuditQuery.builder()
            .targetType(targetType)
            .targetId(targetId)
            .build());
    }
    
    @Override
    public List<AuditEvent> getByTimeRange(Instant from, Instant to) throws Exception {
        return query(AuditQuery.builder()
            .from(from)
            .to(to)
            .build());
    }
    
    @Override
    public AuditStats getStats() throws Exception {
        return getStats(null);
    }
    
    @Override
    public AuditStats getStats(String userId) throws Exception {
        String cacheKey = userId != null ? userId : "all";
        return statsCache.computeIfAbsent(cacheKey, k -> {
            List<AuditEvent> filtered = events.stream()
                .filter(e -> userId == null || e.principal().id().asString().equals(userId))
                .toList();
            
            long successCount = filtered.stream().filter(e -> e.result() == AuditResult.SUCCESS).count();
            long failureCount = filtered.stream().filter(e -> e.result() == AuditResult.FAILURE).count();
            long deniedCount = filtered.stream().filter(e -> e.result() == AuditResult.DENIED).count();
            long errorCount = filtered.stream().filter(e -> e.result() == AuditResult.ERROR).count();
            
            Map<String, Long> actionCounts = filtered.stream()
                .collect(Collectors.groupingBy(AuditEvent::action, Collectors.counting()));
            
            Map<String, Long> userCounts = filtered.stream()
                .collect(Collectors.groupingBy(e -> e.principal().id().asString(), Collectors.counting()));
            
            Map<String, Long> targetCounts = filtered.stream()
                .filter(e -> e.targetType() != null)
                .collect(Collectors.groupingBy(AuditEvent::targetType, Collectors.counting()));
            
            Instant from = filtered.isEmpty() ? Instant.now() : filtered.stream()
                .map(AuditEvent::timestamp)
                .min(Instant::compareTo)
                .orElse(Instant.now());
            
            Instant to = filtered.isEmpty() ? Instant.now() : filtered.stream()
                .map(AuditEvent::timestamp)
                .max(Instant::compareTo)
                .orElse(Instant.now());
            
            return new AuditStats(
                filtered.size(),
                successCount,
                failureCount,
                deniedCount,
                errorCount,
                actionCounts,
                userCounts,
                targetCounts,
                from,
                to
            );
        });
    }
    
    @Override
    public AuditStats getStats(String action, Period period) throws Exception {
        Instant now = Instant.now();
        Instant from = now.minus(period.getDuration());
        
        List<AuditEvent> filtered = events.stream()
            .filter(e -> e.action().equals(action))
            .filter(e -> e.timestamp().isAfter(from))
            .toList();
        
        long successCount = filtered.stream().filter(e -> e.result() == AuditResult.SUCCESS).count();
        long failureCount = filtered.stream().filter(e -> e.result() == AuditResult.FAILURE).count();
        long deniedCount = filtered.stream().filter(e -> e.result() == AuditResult.DENIED).count();
        long errorCount = filtered.stream().filter(e -> e.result() == AuditResult.ERROR).count();
        
        return new AuditStats(
            filtered.size(),
            successCount,
            failureCount,
            deniedCount,
            errorCount,
            Map.of(),
            Map.of(),
            Map.of(),
            from,
            now
        );
    }
    
    @Override
    public void export(String format, Path path) throws Exception {
        export(format, AuditQuery.builder().build(), path);
    }
    
    @Override
    public void export(String format, AuditQuery query, Path path) throws Exception {
        List<AuditEvent> results = query(query);
        
        if ("json".equalsIgnoreCase(format)) {
            // Export as JSON
            try (FileWriter writer = new FileWriter(path.toFile())) {
                new ObjectMapper().writeValue(writer, results);
            }
        } else if ("csv".equalsIgnoreCase(format)) {
            // Export as CSV
            try (FileWriter writer = new FileWriter(path.toFile())) {
                writer.write("Timestamp,Action,User,TargetType,TargetId,Result,Details\n");
                for (AuditEvent event : results) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                        event.timestamp(),
                        event.action(),
                        event.principal().username(),
                        event.targetType(),
                        event.targetId(),
                        event.result(),
                        event.details() != null ? event.details().replace(",", ";") : ""
                    ));
                }
            }
        }
    }
    
    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            createAuditTable();
            initialized = true;
        }
    }
    
    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void createAuditTable() {
        try {
            if (!db.tableExists("wayang_audit")) {
                String sql = """
                    CREATE TABLE wayang_audit (
                        id VARCHAR(36) PRIMARY KEY,
                        action VARCHAR(255) NOT NULL,
                        user_id VARCHAR(36) NOT NULL,
                        username VARCHAR(255) NOT NULL,
                        target_type VARCHAR(255),
                        target_id VARCHAR(36),
                        target_name VARCHAR(255),
                        result VARCHAR(50) NOT NULL,
                        details TEXT,
                        ip_address VARCHAR(45),
                        user_agent TEXT,
                        attributes JSONB,
                        timestamp TIMESTAMP NOT NULL
                    )
                """;
                db.update(sql);
            }
        } catch (SQLException e) {
            // Table might already exist
        }
    }
    
    private void saveToDatabase(AuditEvent event) throws SQLException {
        String sql = """
            INSERT INTO wayang_audit 
            (id, action, user_id, username, target_type, target_id, target_name, 
             result, details, ip_address, user_agent, attributes, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
        """;
        
        ObjectMapper mapper = new ObjectMapper();
        String attributesJson = mapper.writeValueAsString(event.attributes());
        
        db.update(sql, 
            event.id(),
            event.action(),
            event.principal().id().asString(),
            event.principal().username(),
            event.targetType(),
            event.targetId(),
            event.targetName(),
            event.result().name(),
            event.details(),
            event.ipAddress(),
            event.userAgent(),
            attributesJson,
            event.timestamp()
        );
    }
}