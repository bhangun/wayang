package tech.kayys.wayang.assistant.agent.intent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.assistant.agent.ConversationSession;

import java.util.List;

/**
 * Classifies user intent for the Wayang Assistant.
 */
@ApplicationScoped
public class AssistantIntentClassifier {

    private static final Logger LOG = Logger.getLogger(AssistantIntentClassifier.class);

    @Inject
    Instance<GollekInferenceService> inferenceService;

    public String detectIntent(String message, ConversationSession session) {
        String lower = message.toLowerCase();
        if (lower.contains("bug") || lower.contains("crash") || (lower.contains("error") && lower.contains("report"))) return "BUG";
        if (lower.contains("analytic") || lower.contains("metric") || lower.contains("status")) return "ANALYTICS";
        if (lower.contains("troubleshoot") || lower.contains("help with error")) return "TROUBLESHOOT";
        if (lower.contains("search") || lower.contains("where is") || lower.contains("how to")) return "SEARCH";
        
        // Use a quick non-streaming LLM classification if possible
        if (inferenceService != null && inferenceService.isResolvable()) {
            try {
                var res = inferenceService.get().infer(AgentInferenceRequest.builder()
                        .systemPrompt("Classify user intent into one word: BUG, ANALYTICS, TROUBLESHOOT, SEARCH, or CHAT.")
                        .userPrompt(message)
                        .maxTokens(10)
                        .build());
                String classified = res.getContent().trim().toUpperCase().replaceAll("[^A-Z]", "");
                if (List.of("BUG", "ANALYTICS", "TROUBLESHOOT", "SEARCH", "CHAT").contains(classified)) {
                    return classified;
                }
            } catch (Exception e) {
                LOG.warn("Intent classification failed, falling back to keywords", e);
            }
        }
        return "CHAT";
    }
}
