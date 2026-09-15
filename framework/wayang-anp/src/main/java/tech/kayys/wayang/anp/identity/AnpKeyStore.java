package tech.kayys.wayang.anp.identity;

import java.util.Optional;

/**
 * SPI for enterprise-grade management of DID:WBA signing keys.
 *
 * <p>Implementations may load keys from:
 * <ul>
 *   <li>In-memory / classpath configuration (dev / testing)</li>
 *   <li>PKCS#12 / JKS keystore file</li>
 *   <li>HashiCorp Vault / Cloud KMS / HSM (production)</li>
 * </ul>
 */
public interface AnpKeyStore {

    /** Retrieves the key pair associated with a specific key identifier. */
    Optional<AnpIdentityKeyPair> getKey(String keyId);

    /** Resolves the default signing key ID for the given agent. */
    Optional<String> getAgentKeyId(String agentId);

    /** Stores or updates a key pair. */
    void storeKey(String agentId, AnpIdentityKeyPair keyPair);
}
