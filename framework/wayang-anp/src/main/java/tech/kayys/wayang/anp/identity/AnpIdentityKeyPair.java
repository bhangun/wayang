package tech.kayys.wayang.anp.identity;

import java.util.Arrays;
import java.util.Objects;

/**
 * Cryptographic key material held locally for signing and verifying ANP messages.
 */
public record AnpIdentityKeyPair(
        String keyId,
        String algorithm,
        byte[] privateKeyBytes,
        byte[] publicKeyBytes
) {
    public AnpIdentityKeyPair {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(algorithm, "algorithm");
        privateKeyBytes = privateKeyBytes != null ? Arrays.copyOf(privateKeyBytes, privateKeyBytes.length) : null;
        publicKeyBytes = publicKeyBytes != null ? Arrays.copyOf(publicKeyBytes, publicKeyBytes.length) : null;
    }

    public boolean canSign() {
        return privateKeyBytes != null && privateKeyBytes.length > 0;
    }
}
