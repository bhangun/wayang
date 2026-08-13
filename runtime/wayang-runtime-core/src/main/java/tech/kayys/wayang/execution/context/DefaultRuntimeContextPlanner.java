package tech.kayys.wayang.execution.context;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.context.Document;
import tech.kayys.wayang.execution.ExecutionBudget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link RuntimeContextPlanner}.
 *
 * <p>Iterates all available {@link ContextProvider} instances in order,
 * merging their {@link ContextData} into a unified context window while
 * respecting the budget's token ceiling. Providers that exceed the remaining
 * budget are skipped (not truncated), keeping the logic simple and auditable.</p>
 */
public class DefaultRuntimeContextPlanner implements RuntimeContextPlanner {

    private static final Logger LOGGER = Logger.getLogger(DefaultRuntimeContextPlanner.class.getName());

    @Override
    public RuntimeContextPlan planContext(AgentContext context, ExecutionBudget budget, List<ContextProvider> providers) {
        long maxTokens = budget != null ? budget.contextTokens() : 4096L;
        long currentTokens = 0;

        List<Document> allDocuments = new ArrayList<>();
        List<Object>   allMemories  = new ArrayList<>();
        List<Object>   allKnowledge = new ArrayList<>();
        List<String>   contributors = new ArrayList<>();
        StringBuilder  rationale    = new StringBuilder("Context Assembly:\n");

        if (providers == null || providers.isEmpty()) {
            rationale.append("- No providers configured.\n");
            return new PlanImpl(ContextData.empty(), 0, List.of(), rationale.toString());
        }

        for (ContextProvider provider : providers) {
            if (!provider.isAvailable(context)) {
                continue;
            }
            try {
                ContextData pd = provider.load(context);
                if (pd == null || pd.isEmpty()) {
                    continue;
                }
                long est = estimateTokens(pd);
                if (currentTokens + est > maxTokens) {
                    rationale.append("- Skipped [").append(provider.getClass().getSimpleName())
                             .append("]: budget exceeded\n");
                    continue;
                }
                allDocuments.addAll(pd.documents());
                allMemories.addAll(pd.memories());
                allKnowledge.addAll(pd.knowledge());
                currentTokens += est;
                contributors.add(provider.getClass().getSimpleName());
                rationale.append("- Included [").append(provider.getClass().getSimpleName())
                         .append("] ~").append(est).append(" tokens\n");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Context provider failed: " + provider.getClass().getSimpleName(), e);
            }
        }

        ContextData merged = ContextData.empty()
            .withDocuments(allDocuments)
            .withMemories(allMemories)
            .withKnowledge(allKnowledge);

        return new PlanImpl(merged, currentTokens, contributors, rationale.toString());
    }

    // 1 token ≈ 4 characters (rough heuristic)
    private long estimateTokens(ContextData data) {
        long chars = 0;
        if (data.documents() != null) {
            for (Document d : data.documents()) {
                if (d != null && d.content() != null) chars += d.content().length();
            }
        }
        if (data.memories() != null) chars += data.memories().toString().length();
        if (data.knowledge() != null) chars += data.knowledge().toString().length();
        return Math.max(chars / 4, 0);
    }

    // -----------------------------------------------------------------
    private static class PlanImpl implements RuntimeContextPlan {
        private final ContextData contextData;
        private final long tokenUsage;
        private final List<String> contributors;
        private final String rationale;

        PlanImpl(ContextData contextData, long tokenUsage, List<String> contributors, String rationale) {
            this.contextData  = contextData;
            this.tokenUsage   = tokenUsage;
            this.contributors = List.copyOf(contributors);
            this.rationale    = rationale;
        }

        @Override public ContextData getContextData()         { return contextData; }
        @Override public long getTokenUsage()                 { return tokenUsage; }
        @Override public List<String> getContributingProviders() { return contributors; }
        @Override public Optional<String> getAssemblyRationale() { return Optional.ofNullable(rationale); }
    }
}
