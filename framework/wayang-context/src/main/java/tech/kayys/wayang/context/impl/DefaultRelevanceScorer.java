package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.RelevanceScorer;
import tech.kayys.wayang.context.api.model.ReachabilityResult;

import java.nio.file.Path;
import java.util.Set;

/**
 * score = 1/hop, boosted by distinct call-site count and by how many members
 * are confidently attributed as used. Closer + more-referenced + more-precisely-
 * understood files earn the richer tier first when budget is tight.
 */
public final class DefaultRelevanceScorer implements RelevanceScorer {

    private static final double CALL_SITE_WEIGHT = 0.1;
    private static final double USED_MEMBER_WEIGHT = 0.05;

    @Override
    public double score(Path file, ReachabilityResult reachability) {
        Integer hop = reachability.reachable().get(file);
        if (hop == null || hop <= 0) return 0.0;

        double hopScore = 1.0 / hop;
        int callSites = reachability.callSiteCounts().getOrDefault(file, 0);
        int usedMemberCount = reachability.usedMembers().getOrDefault(file, Set.of()).size();

        return hopScore + CALL_SITE_WEIGHT * callSites + USED_MEMBER_WEIGHT * usedMemberCount;
    }
}
