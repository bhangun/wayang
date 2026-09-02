package tech.kayys.wayang.knowledge.exchange.envelope;

public interface KnowledgeEvidenceExchangeMessageAuthenticator {

    KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm algorithm();

    String keyId();

    String keyVersion();

    byte[] authenticate(byte[] message);

    boolean verify(
            byte[] message,
            byte[] authentication
    );
}
