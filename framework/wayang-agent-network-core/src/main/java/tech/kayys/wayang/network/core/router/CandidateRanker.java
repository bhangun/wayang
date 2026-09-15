package tech.kayys.wayang.network.core.router;

import java.util.Comparator;
import java.util.List;

public final class CandidateRanker {

    private CandidateRanker() {}

    public static List<NetworkCandidate> rank(List<NetworkCandidate> candidates) {
        if (candidates == null) return List.of();
        return candidates.stream()
                .sorted(Comparator.comparingInt(NetworkCandidate::priority).reversed())
                .toList();
    }
}
