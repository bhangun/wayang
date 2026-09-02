package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.List;
import java.util.Map;

public final class DefaultKnowledgeEvidencePartialVerificationService
        implements KnowledgeEvidencePartialVerificationService {

    private final KnowledgeEvidenceMerkleProofVerifier verifier;

    public DefaultKnowledgeEvidencePartialVerificationService(
            KnowledgeEvidenceMerkleProofVerifier verifier
    ) {
        this.verifier = verifier;
    }

    @Override
    public KnowledgeEvidencePartialVerificationResult verify(
            KnowledgeEvidenceMerkleProof proof
    ) {
        if (proof == null) {
            return new KnowledgeEvidencePartialVerificationResult(
                    false, null, null, List.of(), List.of("Proof is null"), Map.of()
            );
        }

        boolean verified = verifier.verify(proof);

        return new KnowledgeEvidencePartialVerificationResult(
                verified,
                proof.rootHash(),
                proof.leafId(),
                verified ? List.of(proof.leafId()) : List.of(),
                verified ? List.of() : List.of("Merkle proof verification failed"),
                Map.of()
        );
    }
}
