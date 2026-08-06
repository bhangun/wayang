package tech.kayys.wayang.spi.output;
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
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.agent.AgentContext;


/**
 * Output Provider - sends output to various destinations.
 */
public interface OutputProvider extends Extension {
    
    /**
     * Send output
     */
    void send(OutputResult result) throws Exception;
    
    /**
     * Send with execution context
     */
    default void send(OutputResult result, AgentContext context) throws Exception {
        send(result);
    }
    
    /**
     * Send streaming output
     */
    default void sendStreaming(StreamingOutput output, OutputStreamHandler handler) throws Exception {
        throw new UnsupportedOperationException("Streaming not supported");
    }
    
    /**
     * Send asynchronously
     */
    default CompletableFuture<Void> sendAsync(OutputResult result) {
        return CompletableFuture.runAsync(() -> {
            try {
                send(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get supported output types
     */
    default Set<OutputType> getSupportedTypes() {
        return Set.of(OutputType.TEXT);
    }
}