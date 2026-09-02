package tech.kayys.wayang.knowledge.exchange.envelope;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Objects;

public final class Ed25519KnowledgeEvidenceExchangeAuthenticator
        implements KnowledgeEvidenceExchangeMessageAuthenticator {

    private final String keyId;
    private final String keyVersion;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Ed25519KnowledgeEvidenceExchangeAuthenticator(
            String keyId,
            String keyVersion,
            PrivateKey privateKey,
            PublicKey publicKey
    ) {
        this.keyId = Objects.requireNonNull(keyId, "keyId");
        this.keyVersion = Objects.requireNonNull(keyVersion, "keyVersion");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm algorithm() {
        return KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm.ED25519;
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
        if (privateKey == null) {
            throw new IllegalStateException("Private key not available for signing");
        }
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(message);
            return signature.sign();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign evidence exchange message", e);
        }
    }

    @Override
    public boolean verify(byte[] message, byte[] authentication) {
        if (publicKey == null) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(authentication);
        } catch (Exception e) {
            return false;
        }
    }
}
