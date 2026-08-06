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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tech.kayys.wayang.extension.Id;


/**
 * Message - complete message model
 */
public record Message(
    String id,
    MessageRole role,
    String content,
    List<Attachment> attachments,
    List<Citation> citations,
    Map<String, Object> metadata,
    Map<String, Object> functionCall
) {
    public Message {
        Objects.requireNonNull(role, "role cannot be null");
        Objects.requireNonNull(content, "content cannot be null");
        if (attachments == null) {
            attachments = List.of();
        }
        if (citations == null) {
            citations = List.of();
        }
        if (metadata == null) {
            metadata = Map.of();
        }
        if (functionCall == null) {
            functionCall = Map.of();
        }
    }
    
    public static Message system(String content) {
        return new Message(Id.random().asString(), MessageRole.SYSTEM, content, 
            List.of(), List.of(), Map.of(), Map.of());
    }
    
    public static Message user(String content) {
        return new Message(Id.random().asString(), MessageRole.USER, content, 
            List.of(), List.of(), Map.of(), Map.of());
    }
    
    public static Message assistant(String content) {
        return new Message(Id.random().asString(), MessageRole.ASSISTANT, content, 
            List.of(), List.of(), Map.of(), Map.of());
    }
    
    public static Message tool(String content) {
        return new Message(Id.random().asString(), MessageRole.TOOL, content, 
            List.of(), List.of(), Map.of(), Map.of());
    }
    
    public Message withAttachment(Attachment attachment) {
        List<Attachment> newAttachments = new ArrayList<>(attachments);
        newAttachments.add(attachment);
        return new Message(id, role, content, newAttachments, citations, metadata, functionCall);
    }
    
    public Message withCitation(Citation citation) {
        List<Citation> newCitations = new ArrayList<>(citations);
        newCitations.add(citation);
        return new Message(id, role, content, attachments, newCitations, metadata, functionCall);
    }
}