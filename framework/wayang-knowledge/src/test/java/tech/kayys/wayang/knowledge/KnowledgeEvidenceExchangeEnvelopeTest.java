package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.envelope.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceExchangeEnvelopeTest {

    @Test
    void testEd25519MessageSigningAndVerification() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();

        Ed25519KnowledgeEvidenceExchangeAuthenticator authenticator =
                new Ed25519KnowledgeEvidenceExchangeAuthenticator("key-1", "v1", kp.getPrivate(), kp.getPublic());

        InMemoryKnowledgeEvidenceExchangeKeyResolver keyResolver =
                new InMemoryKnowledgeEvidenceExchangeKeyResolver();
        keyResolver.register(authenticator);

        DefaultKnowledgeEvidenceExchangeMessageCanonicalizer canonicalizer =
                new DefaultKnowledgeEvidenceExchangeMessageCanonicalizer();

        DefaultKnowledgeEvidenceExchangeMessageSigner signer =
                new DefaultKnowledgeEvidenceExchangeMessageSigner(authenticator, canonicalizer);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(15));

        KnowledgeEvidenceExchangeSignedEnvelope envelope = signer.sign(
                "req-1", "session-1", "nonce-1", "runtime-1", "runtime-2",
                "tenant-1", "ws-1", "proj-1", "RESPONSE", "msg-fp-123",
                now, expiresAt, now
        );

        assertNotNull(envelope);
        assertNotNull(envelope.authentication());
        assertTrue(envelope.authentication().length > 0);

        DefaultKnowledgeEvidenceExchangeMessageVerifier verifier =
                new DefaultKnowledgeEvidenceExchangeMessageVerifier(keyResolver, canonicalizer);

        KnowledgeEvidenceExchangeMessageAuthenticationStatus status = verifier.verify(
                envelope, null, "runtime-1", "session-1", "req-1", "nonce-1", now
        );

        assertEquals(KnowledgeEvidenceExchangeMessageAuthenticationStatus.AUTHENTICATED, status);
    }

    @Test
    void testHmacMessageAuthentication() {
        byte[] secret = "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8);
        HmacSha256KnowledgeEvidenceExchangeAuthenticator authenticator =
                new HmacSha256KnowledgeEvidenceExchangeAuthenticator("hmac-key", "v1", secret);

        byte[] message = "Test Exchange Message".getBytes(StandardCharsets.UTF_8);
        byte[] mac = authenticator.authenticate(message);

        assertNotNull(mac);
        assertTrue(authenticator.verify(message, mac));

        byte[] tampered = "Tampered Message".getBytes(StandardCharsets.UTF_8);
        assertFalse(authenticator.verify(tampered, mac));
    }
}
