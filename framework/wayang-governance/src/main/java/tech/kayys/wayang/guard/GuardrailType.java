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


/**
 * Guardrail Types
 */
public enum GuardrailType {
    PROMPT_GUARD,
    PII_DETECTION,
    TOXICITY,
    JAILBREAK_DETECTION,
    POLICY_ENGINE,
    HUMAN_APPROVAL,
    CONTENT_FILTER,
    BIAS_DETECTION,
    HALLUCINATION_DETECTION
}