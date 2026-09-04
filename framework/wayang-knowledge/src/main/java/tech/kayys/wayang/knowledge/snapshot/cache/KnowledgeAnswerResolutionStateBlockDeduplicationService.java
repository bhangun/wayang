package tech.kayys.wayang.knowledge.snapshot.cache;

import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateBlockFingerprinter;
import tech.kayys.wayang.knowledge.snapshot.block.Sha256KnowledgeAnswerResolutionStateBlockFingerprinter;

import java.util.Map;
import java.util.Objects;

public final class KnowledgeAnswerResolutionStateBlockDeduplicationService {

    private final KnowledgeAnswerResolutionSharedStateBlockStore store;
    private final KnowledgeAnswerResolutionStateBlockFingerprinter fingerprinter;

    public KnowledgeAnswerResolutionStateBlockDeduplicationService(
            KnowledgeAnswerResolutionSharedStateBlockStore store,
            KnowledgeAnswerResolutionStateBlockFingerprinter fingerprinter) {
        this.store = Objects.requireNonNull(store, "store");
        this.fingerprinter = Objects.requireNonNull(fingerprinter, "fingerprinter");
    }

    public KnowledgeAnswerResolutionStateBlockDeduplicationService(
            KnowledgeAnswerResolutionSharedStateBlockStore store) {
        this(store, new Sha256KnowledgeAnswerResolutionStateBlockFingerprinter());
    }

    public KnowledgeAnswerResolutionSharedStateBlock put(byte[] data, Map<String, String> metadata) {
        byte[] copy = data != null ? data.clone() : new byte[0];
        String digest = fingerprinter.fingerprint(copy);

        KnowledgeAnswerResolutionStateBlockId id = new KnowledgeAnswerResolutionStateBlockId(
                "sha256",
                digest
        );

        KnowledgeAnswerResolutionSharedStateBlock block = new KnowledgeAnswerResolutionSharedStateBlock(
                id,
                copy,
                copy.length,
                System.currentTimeMillis(),
                metadata != null ? metadata : Map.of()
        );

        return store.put(block);
    }
}
