package tech.kayys.wayang.inference;
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

import tech.kayys.wayang.extension.Id;


/**
 * Completion Result - complete result model
 */
public record CompletionResult(
    String id,
    String model,
    List<Choice> choices,
    Usage usage,
    Map<String, Object> metadata,
    long generationTimeMs
) {
    public static CompletionResult of(Choice choice) {
        return new CompletionResult(
            Id.random().asString(),
            null,
            List.of(choice),
            null,
            Map.of(),
            0
        );
    }
    
    public static CompletionResult of(String content) {
        return new CompletionResult(
            Id.random().asString(),
            null,
            List.of(Choice.of(Message.assistant(content))),
            null,
            Map.of(),
            0
        );
    }
    
    public String getContent() {
        return choices.isEmpty() ? null : choices.get(0).message().content();
    }
    
    public Message getFirstMessage() {
        return choices.isEmpty() ? null : choices.get(0).message();
    }
}
