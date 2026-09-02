package tech.kayys.wayang.knowledge.replay;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

public interface KnowledgeDecisionReexecutor {

    KnowledgeDecisionTrace execute(
            KnowledgeReplaySnapshot snapshot,
            KnowledgeReplayContext context);
}
