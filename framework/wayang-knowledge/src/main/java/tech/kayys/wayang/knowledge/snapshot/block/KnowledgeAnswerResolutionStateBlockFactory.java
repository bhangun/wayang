package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.Map;
import java.util.Objects;

public final class KnowledgeAnswerResolutionStateBlockFactory {

    private final KnowledgeAnswerResolutionStateBlockFingerprinter fingerprinter;

    public KnowledgeAnswerResolutionStateBlockFactory(
            KnowledgeAnswerResolutionStateBlockFingerprinter fingerprinter) {
        this.fingerprinter = Objects.requireNonNull(fingerprinter, "fingerprinter");
    }

    public KnowledgeAnswerResolutionStateBlockFactory() {
        this(new Sha256KnowledgeAnswerResolutionStateBlockFingerprinter());
    }

    public KnowledgeAnswerResolutionStateBlock create(
            byte[] data,
            Map<String, String> metadata) {

        byte[] copy = data != null ? data.clone() : new byte[0];
        String fingerprint = fingerprinter.fingerprint(copy);

        return new KnowledgeAnswerResolutionStateBlock(
                "sha256:" + fingerprint,
                "SHA-256",
                fingerprint,
                copy,
                copy.length,
                metadata != null ? metadata : Map.of()
        );
    }
}
