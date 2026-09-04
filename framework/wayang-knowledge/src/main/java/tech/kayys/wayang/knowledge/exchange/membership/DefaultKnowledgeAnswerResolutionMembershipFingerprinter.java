package tech.kayys.wayang.knowledge.exchange.membership;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class DefaultKnowledgeAnswerResolutionMembershipFingerprinter
        implements KnowledgeAnswerResolutionMembershipFingerprinter {

    @Override
    public String fingerprint(KnowledgeAnswerResolutionMembershipSet membershipSet) {
        String canonical = membershipSet.members()
                .stream()
                .map(KnowledgeAnswerResolutionMembership::runtimeId)
                .sorted()
                .reduce("", (a, b) -> a + b + "\n")
                + "|quorum=" + membershipSet.quorum()
                + "|epoch=" + membershipSet.epochId()
                + "|sequence=" + membershipSet.sequence();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to fingerprint membership set", e);
        }
    }
}
