package tech.kayys.wayang.knowledge.replay;

public record KnowledgeReproducibility(
        boolean evidenceReproducible,
        boolean governanceReproducible,
        boolean policyReproducible,
        boolean ruleReproducible,
        boolean decisionReproducible,
        boolean modelOutputReproducible,
        String explanation
) {

    public boolean fullyReproducible() {
        return evidenceReproducible
                && governanceReproducible
                && policyReproducible
                && ruleReproducible
                && decisionReproducible;
    }
}
