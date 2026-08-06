package tech.kayys.wayang.audit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.*;
import java.time.*;

/**
 * Audit Service - Track all system activities
 */
public interface AuditService extends Extension {
    
    // Log audit events
    void log(AuditEvent event) throws Exception;
    void log(String action, Principal principal, Object target) throws Exception;
    void log(String action, Principal principal, Object target, String result) throws Exception;
    void log(String action, Principal principal, Object target, String result, String details) throws Exception;
    void log(String action, Principal principal, Object target, AuditResult result, String details) throws Exception;
    
    // Query audits
    List<AuditEvent> query(AuditQuery query) throws Exception;
    List<AuditEvent> getByUser(String userId) throws Exception;
    List<AuditEvent> getByAction(String action) throws Exception;
    List<AuditEvent> getByTarget(String targetType, String targetId) throws Exception;
    List<AuditEvent> getByTimeRange(Instant from, Instant to) throws Exception;
    
    // Stats
    AuditStats getStats() throws Exception;
    AuditStats getStats(String userId) throws Exception;
    AuditStats getStats(String action, Period period) throws Exception;
    
    // Export
    void export(String format, Path path) throws Exception;
    void export(String format, AuditQuery query, Path path) throws Exception;
}