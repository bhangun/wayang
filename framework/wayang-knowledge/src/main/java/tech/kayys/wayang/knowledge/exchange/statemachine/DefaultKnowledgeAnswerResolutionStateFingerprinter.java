package tech.kayys.wayang.knowledge.exchange.statemachine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;

public final class DefaultKnowledgeAnswerResolutionStateFingerprinter
        implements KnowledgeAnswerResolutionStateFingerprinter {

    private final KnowledgeAnswerResolutionStateCanonicalizer canonicalizer;

    public DefaultKnowledgeAnswerResolutionStateFingerprinter(
            KnowledgeAnswerResolutionStateCanonicalizer canonicalizer) {
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
    }

    public DefaultKnowledgeAnswerResolutionStateFingerprinter() {
        this(new DefaultKnowledgeAnswerResolutionStateCanonicalizer());
    }

    @Override
    public String fingerprint(KnowledgeAnswerResolutionState state) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = canonicalizer.canonicalize(state).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
