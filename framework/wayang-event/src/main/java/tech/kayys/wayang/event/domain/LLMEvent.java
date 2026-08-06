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

import tech.kayys.wayang.event.EventPayload;
import tech.kayys.wayang.resource.ResourceType.Message;

/**
 * LLM Events
 */
public interface LLMEvent extends EventPayload {
    
    record PromptBuilt(
        String promptId,
        String template,
        Map<String, Object> variables,
        String rendered
    ) implements LLMEvent {}
    
    record CompletionStarted(
        String model,
        String executionId,
        List<Message> messages,
        Map<String, Object> parameters
    ) implements LLMEvent {}
    
    record CompletionFinished(
        String model,
        String executionId,
        String response,
        long durationMs,
        int tokens,
        double cost
    ) implements LLMEvent {}
    
    record CompletionFailed(
        String model,
        String executionId,
        String error
    ) implements LLMEvent {}
    
    record StreamingChunk(
        String model,
        String executionId,
        String chunk,
        boolean isFinal
    ) implements LLMEvent {}
}