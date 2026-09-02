package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.replay.KnowledgeFingerprint;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeGovernanceSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeRuntimeSnapshot;

public final class DefaultKnowledgeIntegrityFingerprintProvider implements KnowledgeIntegrityFingerprintProvider {

    @Override
    public String fingerprintKnowledge(String knowledgeId, String versionId) {
        return KnowledgeFingerprint.sha256("knowledge|" + knowledgeId + "|" + versionId);
    }

    @Override
    public String fingerprintPolicy(String policyId, String versionId) {
        return KnowledgeFingerprint.sha256("policy|" + policyId + "|" + versionId);
    }

    @Override
    public String fingerprintRule(String ruleId, String versionId) {
        return KnowledgeFingerprint.sha256("rule|" + ruleId + "|" + versionId);
    }

    @Override
    public String fingerprintGovernance(KnowledgeGovernanceSnapshot governance) {
        return KnowledgeFingerprint.sha256(String.valueOf(governance));
    }

    @Override
    public String fingerprintRuntime(KnowledgeRuntimeSnapshot runtime) {
        return KnowledgeFingerprint.sha256(String.valueOf(runtime));
    }

    @Override
    public String fingerprintLineage(String lineageId) {
        return KnowledgeFingerprint.sha256("lineage|" + lineageId);
    }
}
