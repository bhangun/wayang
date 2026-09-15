package tech.kayys.wayang.network.capability;

import java.util.List;

public record CapabilityMatch(
        MatchStatus status,
        List<String> matchedCapabilities,
        List<String> missingCapabilities
) {

    public CapabilityMatch {
        matchedCapabilities = matchedCapabilities == null ? List.of() : List.copyOf(matchedCapabilities);
        missingCapabilities = missingCapabilities == null ? List.of() : List.copyOf(missingCapabilities);
    }

    public static CapabilityMatch match(List<String> capabilities) {
        return new CapabilityMatch(MatchStatus.MATCH, capabilities, List.of());
    }

    public static CapabilityMatch noMatch(List<String> missing) {
        return new CapabilityMatch(MatchStatus.NO_MATCH, List.of(), missing);
    }

    public static CapabilityMatch partial(List<String> matched, List<String> missing) {
        return new CapabilityMatch(MatchStatus.PARTIAL, matched, missing);
    }
}
