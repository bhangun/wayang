package tech.kayys.wayang.knowledge.exchange.binding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Sha256KnowledgeEvidenceExchangeResponseFingerprinter
        implements KnowledgeEvidenceExchangeResponseFingerprinter {

    private final KnowledgeEvidenceExchangeResponseBindingCanonicalizer canonicalizer;

    public Sha256KnowledgeEvidenceExchangeResponseFingerprinter(
            KnowledgeEvidenceExchangeResponseBindingCanonicalizer canonicalizer
    ) {
        this.canonicalizer = canonicalizer;
    }

    @Override
    public String fingerprint(KnowledgeEvidenceExchangeResponseBinding binding) {
        String canonical = canonicalizer.canonicalize(binding);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder("sha256:");
            for (byte value : hash) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
