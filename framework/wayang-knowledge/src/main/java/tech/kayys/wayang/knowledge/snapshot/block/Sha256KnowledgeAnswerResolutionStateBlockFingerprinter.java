package tech.kayys.wayang.knowledge.snapshot.block;

import java.security.MessageDigest;
import java.util.HexFormat;

public final class Sha256KnowledgeAnswerResolutionStateBlockFingerprinter
        implements KnowledgeAnswerResolutionStateBlockFingerprinter {

    @Override
    public String fingerprint(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data != null ? data : new byte[0]);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to fingerprint state block", e);
        }
    }
}
