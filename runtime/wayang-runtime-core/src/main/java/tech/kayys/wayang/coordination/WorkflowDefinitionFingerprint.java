package tech.kayys.wayang.coordination;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Immutable SHA-256 fingerprint representing a canonical coordination workflow topology.
 */
public record WorkflowDefinitionFingerprint(String value) {

    public WorkflowDefinitionFingerprint {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("fingerprint must not be blank");
        }
    }

    public static WorkflowDefinitionFingerprint sha256(String canonical) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }

            return new WorkflowDefinitionFingerprint(result.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    public String shortValue() {
        return value.substring(0, Math.min(16, value.length()));
    }
}
