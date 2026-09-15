package tech.kayys.wayang.anp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.anp.auth.AnpAuthenticator;
import tech.kayys.wayang.anp.auth.AnpAuthVerifier;
import tech.kayys.wayang.anp.auth.AnpHttpSignature;
import tech.kayys.wayang.anp.identity.AnpIdentityKeyPair;
import tech.kayys.wayang.anp.identity.InMemoryAnpKeyStore;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AnpHttpSignatureTest {

    private InMemoryAnpKeyStore keyStore;
    private AnpAuthenticator authenticator;
    private AnpAuthVerifier verifier;
    private AnpIdentityKeyPair keyPair;

    @BeforeEach
    void setUp() {
        keyStore = new InMemoryAnpKeyStore();
        authenticator = new AnpAuthenticator();
        verifier = new AnpAuthVerifier(keyStore);

        byte[] secret = "test-secret-key-material-32bytes!".getBytes(StandardCharsets.UTF_8);
        keyPair = new AnpIdentityKeyPair(
                "did:wba:example.com:agent1#key-1",
                "hmac-sha256",
                secret,
                secret
        );
        keyStore.storeKey("agent1", keyPair);
    }

    @Test
    void signsAndVerifiesValidRequest() {
        AnpHttpSignature sig = authenticator.sign("POST", "/message", "example.com", keyPair);

        assertNotNull(sig);
        assertEquals("did:wba:example.com:agent1#key-1", sig.keyId());

        boolean valid = verifier.verify("POST", "/message", "example.com", sig);
        assertTrue(valid, "Signature should be valid for authentic request");
    }

    @Test
    void rejectsTamperedPathOrMethod() {
        AnpHttpSignature sig = authenticator.sign("POST", "/message", "example.com", keyPair);

        assertFalse(verifier.verify("GET", "/message", "example.com", sig), "Method tamper should fail");
        assertFalse(verifier.verify("POST", "/tampered", "example.com", sig), "Path tamper should fail");
        assertFalse(verifier.verify("POST", "/message", "otherhost.com", sig), "Host tamper should fail");
    }

    @Test
    void signatureHeaderRoundTrip() {
        AnpHttpSignature sig = authenticator.sign("POST", "/message", "example.com", keyPair);
        String header = sig.toHeaderValue();

        AnpHttpSignature parsed = AnpHttpSignature.parseHeader(header);
        assertEquals(sig.keyId(), parsed.keyId());
        assertEquals(sig.algorithm(), parsed.algorithm());
        assertArrayEquals(sig.signatureBytes(), parsed.signatureBytes());
    }
}
