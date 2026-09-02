package tech.kayys.wayang.knowledge.governance;

import java.util.Set;

public record GovernanceRule(
        String id,
        KnowledgeClassification classification,
        Set<String> allowedScopes,
        KnowledgeTrustLevel minTrustLevel
) {

    public GovernanceRule {
        allowedScopes = allowedScopes == null ? Set.of() : Set.copyOf(allowedScopes);
        classification = classification == null ? KnowledgeClassification.INTERNAL : classification;
        minTrustLevel = minTrustLevel == null ? KnowledgeTrustLevel.REVIEWED : minTrustLevel;
    }
}
