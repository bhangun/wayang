package tech.kayys.wayang.knowledge.exchange.statemachine;

import java.util.ArrayList;
import java.util.Comparator;

public final class DefaultKnowledgeAnswerResolutionStateCanonicalizer
        implements KnowledgeAnswerResolutionStateCanonicalizer {

    @Override
    public String canonicalize(KnowledgeAnswerResolutionState state) {
        if (state == null) {
            return "version=1\nempty=true\n";
        }

        StringBuilder out = new StringBuilder();
        out.append("version=1\n");
        out.append("lastAppliedIndex=").append(state.lastAppliedIndex()).append('\n');
        out.append("currentTerm=").append(state.currentTerm()).append('\n');
        out.append("currentEpochId=").append(nullSafe(state.currentEpochId())).append('\n');

        var consensusIds = new ArrayList<>(state.consensuses().keySet());
        consensusIds.sort(Comparator.naturalOrder());

        for (String id : consensusIds) {
            var c = state.consensuses().get(id);
            if (c != null) {
                out.append("consensus=")
                        .append(id).append('|')
                        .append(nullSafe(c.keyFingerprint())).append('|')
                        .append(nullSafe(c.resolutionFingerprint())).append('|')
                        .append(nullSafe(c.dependencyFingerprint())).append('|')
                        .append(nullSafe(c.epochId())).append('|')
                        .append(c.status()).append('|')
                        .append(c.activatedAt()).append('|')
                        .append(c.expiresAt()).append('\n');
            }
        }

        state.revokedConsensusIds().stream().sorted().forEach(id ->
                out.append("revoked=").append(id).append('\n'));

        state.activeLeaseIds().stream().sorted().forEach(id ->
                out.append("lease=").append(id).append('\n'));

        state.activeRuntimeIds().stream().sorted().forEach(id ->
                out.append("runtime=").append(id).append('\n'));

        state.entries().keySet().stream().sorted().forEach(k ->
                out.append("entry=").append(k).append('\n'));

        return out.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
