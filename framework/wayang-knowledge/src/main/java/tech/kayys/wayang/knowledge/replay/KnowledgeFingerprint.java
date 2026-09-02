package tech.kayys.wayang.knowledge.replay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class KnowledgeFingerprint {

    private KnowledgeFingerprint() {}

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate fingerprint", e);
        }
    }
}
