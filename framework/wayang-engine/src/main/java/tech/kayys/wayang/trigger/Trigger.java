package tech.kayys.wayang.trigger;
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

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.event.Event;
import tech.kayys.wayang.service.ExecutionService;

/**
 * Trigger - starts execution based on events.
 */
public interface Trigger extends Extension {
    
    /**
     * Get trigger type
     */
    TriggerType triggerType();
    
    /**
     * Check if event is supported
     */
    boolean supports(Event event);
    
    /**
     * Handle trigger
     */
    void onTrigger(Event event, ExecutionService executionService) throws Exception;
    
    /**
     * Start trigger
     */
    default void start() throws Exception {
        // Start listening
    }
    
    /**
     * Stop trigger
     */
    default void stop() throws Exception {
        // Stop listening
    }
    
    /**
     * Check if running
     */
    default boolean isRunning() {
        return false;
    }
}