package tech.kayys.wayang.knowledge.exchange.binding;

import java.util.UUID;

public final class UuidKnowledgeEvidenceExchangeResponseIdGenerator
        implements KnowledgeEvidenceExchangeResponseIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
