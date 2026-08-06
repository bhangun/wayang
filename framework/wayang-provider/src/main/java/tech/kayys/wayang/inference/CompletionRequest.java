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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.ResourceType.Tool;


/**
 * Completion Request - complete request model
 */
public record CompletionRequest(
    String id,
    String model,
    List<Message> messages,
    Map<String, Object> parameters,
    List<Tool> tools,
    String responseFormat,
    Map<String, Object> metadata,
    double temperature,
    int maxTokens,
    double topP,
    List<String> stopSequences,
    boolean stream
) {
    public static CompletionRequestBuilder builder() {
        return new CompletionRequestBuilder();
    }
    
    public static CompletionRequest of(String model, List<Message> messages) {
        return new CompletionRequest(
            Id.random().asString(),
            model,
            messages,
            Map.of(),
            List.of(),
            null,
            Map.of(),
            0.7,
            1000,
            1.0,
            List.of(),
            false
        );
    }
    
    public CompletionRequest withMessage(Message message) {
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(message);
        return new CompletionRequest(id, model, newMessages, parameters, tools, 
            responseFormat, metadata, temperature, maxTokens, topP, stopSequences, stream);
    }
    
    public CompletionRequest withParameter(String key, Object value) {
        Map<String, Object> newParams = new HashMap<>(parameters);
        newParams.put(key, value);
        return new CompletionRequest(id, model, messages, newParams, tools, 
            responseFormat, metadata, temperature, maxTokens, topP, stopSequences, stream);
    }
    
    public static class CompletionRequestBuilder {
        private String id;
        private String model;
        private final List<Message> messages = new ArrayList<>();
        private final Map<String, Object> parameters = new HashMap<>();
        private final List<Tool> tools = new ArrayList<>();
        private String responseFormat;
        private final Map<String, Object> metadata = new HashMap<>();
        private double temperature = 0.7;
        private int maxTokens = 1000;
        private double topP = 1.0;
        private final List<String> stopSequences = new ArrayList<>();
        private boolean stream = false;
        
        public CompletionRequestBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public CompletionRequestBuilder model(String model) {
            this.model = model;
            return this;
        }
        
        public CompletionRequestBuilder messages(List<Message> messages) {
            this.messages.addAll(messages);
            return this;
        }
        
        public CompletionRequestBuilder message(Message message) {
            this.messages.add(message);
            return this;
        }
        
        public CompletionRequestBuilder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
        
        public CompletionRequestBuilder tool(Tool tool) {
            this.tools.add(tool);
            return this;
        }
        
        public CompletionRequestBuilder responseFormat(String responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }
        
        public CompletionRequestBuilder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public CompletionRequestBuilder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public CompletionRequestBuilder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }
        
        public CompletionRequestBuilder topP(double topP) {
            this.topP = topP;
            return this;
        }
        
        public CompletionRequestBuilder stopSequence(String stopSequence) {
            this.stopSequences.add(stopSequence);
            return this;
        }
        
        public CompletionRequestBuilder stream(boolean stream) {
            this.stream = stream;
            return this;
        }
        
        public CompletionRequest build() {
            if (id == null) {
                id = Id.random().asString();
            }
            return new CompletionRequest(id, model, messages, parameters, tools, 
                responseFormat, metadata, temperature, maxTokens, topP, 
                stopSequences, stream);
        }
    }
}
