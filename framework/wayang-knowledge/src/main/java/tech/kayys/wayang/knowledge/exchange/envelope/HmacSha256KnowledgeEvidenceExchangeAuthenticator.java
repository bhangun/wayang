package tech.kayys.wayang.knowledge.exchange.envelope;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Objects;

public final class HmacSha256KnowledgeEvidenceExchangeAuthenticator
        implements KnowledgeEvidenceExchangeMessageAuthenticator {

    private final String keyId;
    private final String keyVersion;
    private final byte[] secret;

    public HmacSha256KnowledgeEvidenceExchangeAuthenticator(
            String keyId,
            String keyVersion,
            byte[] secret
    ) {
        this.keyId = Objects.requireNonNull(keyId, "keyId");
        this.keyVersion = Objects.requireNonNull(keyVersion, "keyVersion");

        if (secret == null || secret.length < 32) {
            throw new IllegalArgumentException("HMAC secret must contain at least 32 bytes");
        }

        this.secret = secret.clone();
    }

    @Override
    public KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm algorithm() {
        return KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm.HMAC_SHA256;
    }

    @Override
    public String keyId() {
        return keyId;
    }

    @Override
    public String keyVersion() {
        return keyVersion;
    }

    @Override
    public byte[] authenticate(byte[] message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(message);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate HMAC", e);
        }
    }

    @Override
    public boolean verify(byte[] message, byte[] authentication) {
        if (message == null || authentication == null) {
            return false;
        }
        return MessageDigest.isEqual(authenticate(message), authentication);
    }
}
