package tech.kayys.wayang.event.domain;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import tech.kayys.wayang.event.EventPayload;

/**
 * Policy Events
 */
public interface PolicyEvent extends EventPayload {
    
    record PolicyChecked(
        String policyId,
        String resource,
        String action,
        String principal,
        boolean allowed
    ) implements PolicyEvent {}
    
    record PolicyDenied(
        String policyId,
        String resource,
        String action,
        String principal,
        String reason
    ) implements PolicyEvent {}
    
    record PolicyApproved(
        String policyId,
        String resource,
        String action,
        String principal
    ) implements PolicyEvent {}
}