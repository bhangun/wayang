package tech.kayys.wayang.knowledge.replay;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

public interface KnowledgeReplayContextProvider {

    KnowledgeReplayContext context(
            KnowledgeDecisionTrace trace,
            KnowledgeReplayRequest request);
}
