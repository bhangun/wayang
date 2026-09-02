package tech.kayys.wayang.knowledge.exchange.session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Sha256KnowledgeEvidenceExchangeBindingFingerprinter
        implements KnowledgeEvidenceExchangeBindingFingerprinter {

    private final KnowledgeEvidenceExchangeBindingCanonicalizer canonicalizer;

    public Sha256KnowledgeEvidenceExchangeBindingFingerprinter(
            KnowledgeEvidenceExchangeBindingCanonicalizer canonicalizer
    ) {
        this.canonicalizer = canonicalizer;
    }

    @Override
    public String fingerprint(KnowledgeEvidenceExchangeRequestBinding binding) {
        String canonical = canonicalizer.canonicalize(binding);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder("sha256:");
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
