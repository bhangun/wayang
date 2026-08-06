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


import java.util.List;
import java.util.Map;

import tech.kayys.wayang.definition.SkillDefinition;
import tech.kayys.wayang.event.EventPayload;
import tech.kayys.wayang.resource.Artifact;

/**
 * Skill Events
 */
public interface SkillEvent extends EventPayload {
    
    record SkillStarted(
        SkillDefinition skill,
        String executionId,
        Map<String, Object> inputs
    ) implements SkillEvent {}
    
    record SkillCompleted(
        String executionId,
        List<Artifact> outputs,
        long durationMs
    ) implements SkillEvent {}
    
    record SkillFailed(
        String executionId,
        String error
    ) implements SkillEvent {}
    
    record SkillRetrying(
        String executionId,
        int attempt,
        int maxAttempts,
        String reason
    ) implements SkillEvent {}
}