package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeGovernanceSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeRuntimeSnapshot;

public interface KnowledgeIntegrityFingerprintProvider {

    String fingerprintKnowledge(String knowledgeId, String versionId);

    String fingerprintPolicy(String policyId, String versionId);

    String fingerprintRule(String ruleId, String versionId);

    String fingerprintGovernance(KnowledgeGovernanceSnapshot governance);

    String fingerprintRuntime(KnowledgeRuntimeSnapshot runtime);

    String fingerprintLineage(String lineageId);
}
