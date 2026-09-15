package tech.kayys.wayang.anp.auth;

import tech.kayys.wayang.anp.identity.AnpIdentityKeyPair;
import tech.kayys.wayang.anp.identity.AnpKeyStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;

/**
 * Verifies inbound ANP HTTP signatures against registered agent public keys.
 */
public final class AnpAuthVerifier {

    private final AnpKeyStore keyStore;

    public AnpAuthVerifier(AnpKeyStore keyStore) {
        this.keyStore = Objects.requireNonNull(keyStore, "keyStore");
    }

    /**
     * Verifies an incoming request signature.
     */
    public boolean verify(
            String method,
            String path,
            String host,
            AnpHttpSignature signature) {

        Objects.requireNonNull(signature, "signature");

        Optional<AnpIdentityKeyPair> keyOpt = keyStore.getKey(signature.keyId());
        if (keyOpt.isEmpty()) {
            return false;
        }

        AnpIdentityKeyPair key = keyOpt.get();
        String canonical = AnpAuthenticator.buildCanonicalString(method, path, host);
        byte[] expected = computeExpected(canonical.getBytes(StandardCharsets.UTF_8), key);

        return MessageDigest.isEqual(expected, signature.signatureBytes());
    }

    private byte[] computeExpected(byte[] data, AnpIdentityKeyPair keyPair) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyPair.privateKeyBytes(), "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
