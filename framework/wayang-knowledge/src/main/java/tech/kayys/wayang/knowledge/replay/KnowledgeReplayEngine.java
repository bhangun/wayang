package tech.kayys.wayang.knowledge.replay;

public interface KnowledgeReplayEngine {

    KnowledgeReplayResult replay(KnowledgeReplayRequest request);
}
