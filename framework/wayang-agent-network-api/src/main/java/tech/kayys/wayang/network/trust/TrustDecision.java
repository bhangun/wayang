package tech.kayys.wayang.network.trust;

public record TrustDecision(
        boolean trusted,
        String reason
) {

    public static TrustDecision accept() {
        return new TrustDecision(true, "trusted");
    }

    public static TrustDecision reject(String reason) {
        return new TrustDecision(false, reason);
    }
}
