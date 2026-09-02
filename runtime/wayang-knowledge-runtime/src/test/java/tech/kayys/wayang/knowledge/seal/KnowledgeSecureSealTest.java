package tech.kayys.wayang.knowledge.seal;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityResult;
import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityStatus;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSecureSealTest {

    @Test
    void testEd25519SigningAndVerification() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();

        String keyId = "key-ed25519-primary";
        String keyVersion = "v1";

        Ed25519KnowledgeSnapshotSigner signer = new Ed25519KnowledgeSnapshotSigner(kp.getPrivate(), keyId, keyVersion);
        Ed25519KnowledgeSnapshotSignatureVerifier signatureVerifier = new Ed25519KnowledgeSnapshotSignatureVerifier(
                Map.of(keyId + ":" + keyVersion, kp.getPublic())
        );

        KnowledgeSnapshotSealCanonicalizer canonicalizer = new DefaultKnowledgeSnapshotSealCanonicalizer();
        DefaultKnowledgeSnapshotSecureSealService sealService = new DefaultKnowledgeSnapshotSecureSealService(signer, canonicalizer);
        DefaultKnowledgeSnapshotSealVerificationService verifyService = new DefaultKnowledgeSnapshotSealVerificationService(canonicalizer, signatureVerifier);

        KnowledgeSnapshotId snapshotId = KnowledgeSnapshotId.of("snap-seal-1");
        KnowledgeSnapshotIntegrityResult attestedResult = new KnowledgeSnapshotIntegrityResult(
                "v-1", snapshotId, KnowledgeSnapshotIntegrityStatus.ATTESTED, Instant.now(),
                List.of(), List.of(), List.of(), "fp-expected-1", "fp-expected-1", Map.of()
        );

        KnowledgeSnapshotSecureSeal seal = sealService.seal(attestedResult, "verifier-engine", "1.0");
        assertNotNull(seal);
        assertEquals(KnowledgeSnapshotSealStatus.SEALED, seal.status());
        assertEquals(KnowledgeSnapshotSealAlgorithm.ED25519, seal.algorithm());

        KnowledgeSnapshotSealPayload payload = new KnowledgeSnapshotSealPayload(
                snapshotId.value(), "fp-expected-1", KnowledgeSnapshotIntegrityStatus.ATTESTED.name(),
                "verifier-engine", "1.0", attestedResult.verifiedAt()
        );

        KnowledgeSnapshotSealVerificationResult verResult = verifyService.verify(seal, payload);
        assertTrue(verResult.valid());
        assertEquals(KnowledgeSnapshotSealStatus.VERIFIED, verResult.status());

        // Tampered payload fails verification
        KnowledgeSnapshotSealPayload tamperedPayload = new KnowledgeSnapshotSealPayload(
                snapshotId.value(), "fp-tampered-999", KnowledgeSnapshotIntegrityStatus.ATTESTED.name(),
                "verifier-engine", "1.0", attestedResult.verifiedAt()
        );

        KnowledgeSnapshotSealVerificationResult tamperedVerResult = verifyService.verify(seal, tamperedPayload);
        assertFalse(tamperedVerResult.valid());
        assertEquals(KnowledgeSnapshotSealStatus.INVALID, tamperedVerResult.status());
    }
}
