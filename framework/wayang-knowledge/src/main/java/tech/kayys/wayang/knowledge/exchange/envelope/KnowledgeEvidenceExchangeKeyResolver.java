package tech.kayys.wayang.knowledge.exchange.envelope;

import java.time.Instant;
import java.util.Optional;

public interface KnowledgeEvidenceExchangeKeyResolver {

    Optional<KnowledgeEvidenceExchangeMessageAuthenticator> resolveSigner(
            String keyId,
            String keyVersion,
            Instant now
    );

    Optional<KnowledgeEvidenceExchangeMessageAuthenticator> resolveVerifier(
            String keyId,
            String keyVersion,
            Instant now
    );
}
