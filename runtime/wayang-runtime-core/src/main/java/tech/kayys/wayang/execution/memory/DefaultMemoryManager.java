package tech.kayys.wayang.execution.memory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.AgentContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link MemoryManager} that delegates to a {@link MemoryPolicy}.
 *
 * <p>Retrieval strategy per layer:</p>
 * <ul>
 *   <li><b>WORKING</b> — last 5 working entries (current turn state)</li>
 *   <li><b>EPISODIC</b> — last 3 episodes from the session</li>
 *   <li><b>SEMANTIC</b> — top 5 fact / preference records by relevance</li>
 *   <li><b>PROCEDURAL</b> — top 2 relevant skill/strategy entries</li>
 * </ul>
 */
@ApplicationScoped
public class DefaultMemoryManager implements MemoryManager {

    @Inject
    MemoryPolicy policy;

    @Override
    public List<String> retrieve(AgentContext context, String query, int limit) {
        List<String> results = new ArrayList<>();

        // Working memory — most recent state items
        results.addAll(policy.retrieve(context, MemoryLayer.WORKING, null, 5));

        // Episodic — previous session turns
        results.addAll(policy.retrieve(context, MemoryLayer.EPISODIC, query, 3));

        // Semantic — relevant facts/preferences
        results.addAll(policy.retrieve(context, MemoryLayer.SEMANTIC, query, 5));

        // Procedural — workflow strategies
        results.addAll(policy.retrieve(context, MemoryLayer.PROCEDURAL, query, 2));

        // Trim to limit if needed
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    @Override
    public void writeTurn(AgentContext context, String turnInput, String turnOutput) {
        // Write the turn as an episodic memory
        String episode = "Turn — Input: " + truncate(turnInput, 200)
            + "\nOutput: " + truncate(turnOutput, 400);
        policy.write(context, MemoryLayer.EPISODIC, episode);

        // Update working memory with output
        policy.write(context, MemoryLayer.WORKING, "Last output: " + truncate(turnOutput, 200));
    }

    @Override
    public List<String> workingMemory(AgentContext context) {
        return policy.retrieve(context, MemoryLayer.WORKING, null, Integer.MAX_VALUE);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "…" : s;
    }
}
