package tech.kayys.wayang.context.api;

import tech.kayys.wayang.context.api.model.ReachabilityResult;

import java.nio.file.Path;

/**
 * Scores a reachable file's relevance to the compilation target, purely from
 * signals the resolver already computed mechanically -- hop distance, call
 * site count, member-attribution confidence. Deliberately not a semantic or
 * embedding-based ranker: every score this interface can produce is fully
 * explainable from ReachabilityResult alone, with no separate model call and
 * nothing that could rank a file highly for reasons that can't be shown.
 */
public interface RelevanceScorer {
    double score(Path file, ReachabilityResult reachability);
}
