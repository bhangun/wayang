package com.contextcompiler.core.api.model;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Approximate dependency graph for a target file, plus every blind spot the
 * resolver could not confidently explain. The design principle carried over
 * from the Python original: an incomplete map with explicit warnings beats a
 * complete map that is silently wrong.
 */
public final class ReachabilityResult {

    private final Map<Path, Integer> reachable = new LinkedHashMap<>();
    private final Set<String> unresolvedCalls = new LinkedHashSet<>();
    private final Set<Path> dynamicDispatchFiles = new LinkedHashSet<>();
    private final Map<Path, Set<String>> eventAnnotationHints = new LinkedHashMap<>();
    private final Map<String, List<Path>> nameCollisions = new LinkedHashMap<>();
    private final Map<Path, Set<String>> usedMembers = new LinkedHashMap<>();
    private final Map<Path, Integer> callSiteCounts = new LinkedHashMap<>();

    /** path -> hop distance from the target file. */
    public Map<Path, Integer> reachable() {
        return reachable;
    }

    /**
     * path -> member names (methods/constructors) known, with reasonable
     * confidence, to actually be called somewhere along the reachable chain.
     * Deliberately conservative in one direction only: a name can end up here
     * on a heuristic match that turns out unrelated (over-inclusion, same
     * trade-off the resolver already makes elsewhere), but a member that
     * genuinely is used is never left out. Consumers doing member-level
     * pruning should treat an empty set for a file as "attribution unknown,
     * keep the whole file" rather than "nothing is used."
     */
    public Map<Path, Set<String>> usedMembers() {
        return usedMembers;
    }

    /** path -> number of distinct call sites (across the whole traversal) that referenced it. Used for relevance ranking. */
    public Map<Path, Integer> callSiteCounts() {
        return callSiteCounts;
    }

    /** Bare call names that matched no import and no repo-wide definition. */
    public Set<String> unresolvedCalls() {
        return unresolvedCalls;
    }

    /** Files using reflection (Class.forName / Method.invoke) -- targets may be missing. */
    public Set<Path> dynamicDispatchFiles() {
        return dynamicDispatchFiles;
    }

    /** Files using event-style annotations (@Observes, @ConsumeEvent, ...) -- handlers may be missing. */
    public Map<Path, Set<String>> eventAnnotationHints() {
        return eventAnnotationHints;
    }

    /** Bare method name -> every file declaring a method with that name (name-only resolution). */
    public Map<String, List<Path>> nameCollisions() {
        return nameCollisions;
    }
}
