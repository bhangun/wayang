package tech.kayys.wayang.guard;
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
import tech.kayys.wayang.execution.ExecutionContext;


/**
 * Guardrail Provider - validates safety and compliance.
 */
public interface GuardrailProvider extends Extension {
    
    /**
     * Validate execution context
     */
    GuardrailResult validate(ExecutionContext context) throws Exception;
    
    /**
     * Validate content
     */
    default GuardrailResult validate(ExecutionContext context, Object content) throws Exception {
        return validate(context);
    }
    
    /**
     * Validate asynchronously
     */
    default CompletableFuture<GuardrailResult> validateAsync(ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return validate(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get supported guardrails
     */
    default Set<GuardrailType> getSupportedTypes() {
        return Set.of(GuardrailType.PROMPT_GUARD);
    }
}