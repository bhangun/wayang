package tech.kayys.wayang.execution.context;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.inference.ModelInfo;
import tech.kayys.wayang.resource.Artifact;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default implementation of {@link ContextPlanner}.
 *
 * <p>Strategy:</p>
 * <ol>
 *   <li>Pick budget based on model context window (128k vs 32k heuristic).</li>
 *   <li>Build system prompt from static instructions.</li>
 *   <li>Pull conversation history up to {@code conversationTokens} budget.</li>
 *   <li>Query every registered {@link ContextProvider} for relevant data.</li>
 *   <li>Serialize artifacts from the current run.</li>
 *   <li>Return a {@link ContextPlan}.</li>
 * </ol>
 */
@ApplicationScoped
public class DefaultContextPlanner implements ContextPlanner {

    @Inject
    Instance<ContextProvider> contextProviders;

    @Override
    public ContextPlan plan(AgentContext context, ModelInfo model, ContextBudget budget) {
        ContextBudget effectiveBudget = (budget != null) ? budget : selectBudget(model);

        String systemPrompt = buildSystemPrompt(context);
        String conversationStr = buildConversationStr(context, effectiveBudget);
        String memoryStr = buildMemoryStr(context, effectiveBudget);
        String ragStr = buildRagStr(context, effectiveBudget);
        String artifactStr = buildArtifactStr(context, effectiveBudget);

        return new ContextPlan(
            systemPrompt,
            conversationStr,
            memoryStr,
            ragStr,
            artifactStr,
            effectiveBudget,
            model
        );
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private ContextBudget selectBudget(ModelInfo model) {
        // Heuristic: long-context models get a 128k budget; others get 32k
        if (model != null && model.capabilities().contains("long_context")) {
            return ContextBudget.forWindow128k();
        }
        return ContextBudget.forWindow32k();
    }

    private String buildSystemPrompt(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful AI assistant.\n");
        // If the request carries a system prompt, prepend it
        if (context.request() != null && context.request().systemPrompt() != null) {
            sb.append("\n").append(context.request().systemPrompt());
        }
        return sb.toString();
    }

    private String buildConversationStr(AgentContext context, ContextBudget budget) {
        // Approximate: 1 token ≈ 4 chars. Trim the conversation to stay in budget.
        List<Object> history = context.getVariableAsList("conversation_history", Object.class);
        if (history == null || history.isEmpty()) return "";
        int maxChars = budget.conversationTokens() * 4;
        String full = history.stream().map(Object::toString).collect(Collectors.joining("\n"));
        return full.length() > maxChars ? full.substring(full.length() - maxChars) : full;
    }

    private String buildMemoryStr(AgentContext context, ContextBudget budget) {
        // Memory retrieval is handled by MemoryManager; here we read what was already
        // attached to the context via the "retrieved_memory" variable.
        String memory = context.getVariableAsString("retrieved_memory");
        if (memory == null) return "";
        int maxChars = budget.memoryTokens() * 4;
        return memory.length() > maxChars ? memory.substring(0, maxChars) : memory;
    }

    private String buildRagStr(AgentContext context, ContextBudget budget) {
        if (contextProviders == null) return "";
        int maxChars = budget.ragTokens() * 4;
        StringBuilder sb = new StringBuilder();
        for (ContextProvider provider : contextProviders) {
            if (sb.length() >= maxChars) break;
            try {
                var data = provider.loadWithQuery(context,
                    context.request() != null ? context.request().input() : "");
                if (data != null && data.content() != null) {
                    sb.append(data.content()).append("\n");
                }
            } catch (Exception ignored) {
                // Provider failed — skip and continue with others
            }
        }
        String result = sb.toString();
        return result.length() > maxChars ? result.substring(0, maxChars) : result;
    }

    private String buildArtifactStr(AgentContext context, ContextBudget budget) {
        if (context.artifacts() == null || context.artifacts().isEmpty()) return "";
        int maxChars = budget.artifactTokens() * 4;
        StringBuilder sb = new StringBuilder();
        for (Artifact artifact : context.artifacts()) {
            if (sb.length() >= maxChars) break;
            sb.append("[").append(artifact.type()).append("] ")
              .append(artifact.id().asString()).append("\n");
        }
        return sb.toString();
    }
}
