package tech.kayys.wayang.knowledge.snapshot.pack;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class KnowledgeSnapshotPackageFingerprint {

    private KnowledgeSnapshotPackageFingerprint() {
    }

    public static String sha256(String value) {
        if (value == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate SHA-256", e);
        }
    }

    public static String sha256(byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate SHA-256", e);
        }
    }
}
