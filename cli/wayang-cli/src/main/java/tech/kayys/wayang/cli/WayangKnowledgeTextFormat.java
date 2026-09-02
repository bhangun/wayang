package tech.kayys.wayang.cli;

import java.util.List;
import java.util.Map;

/**
 * Text formatting helpers for Wayang Knowledge and Evidence commands.
 */
public final class WayangKnowledgeTextFormat {

    private WayangKnowledgeTextFormat() {
    }

    public static String formatQueryHeader(String query, int totalHits) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KNOWLEDGE EVIDENCE SEARCH ===\n");
        sb.append("Query: ").append(query).append("\n");
        sb.append("Matches found: ").append(totalHits).append("\n");
        sb.append("----------------------------------------------------------------\n");
        return sb.toString();
    }

    public static String formatResolution(String resolutionId, String decision, String winner, double confidence) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KNOWLEDGE ANSWER CONSENSUS RESOLUTION ===\n");
        sb.append("Resolution ID: ").append(resolutionId).append("\n");
        sb.append("Consensus Decision: ").append(decision).append("\n");
        sb.append("Selected Candidate: ").append(winner).append("\n");
        sb.append("Consensus Confidence: ").append(String.format("%.2f%%", confidence * 100)).append("\n");
        sb.append("Status: COMMITTED & ATTESTED\n");
        return sb.toString();
    }

    public static String formatVerification(String artifactId, boolean valid, String status) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KNOWLEDGE ARTIFACT VERIFICATION ===\n");
        sb.append("Artifact ID: ").append(artifactId).append("\n");
        sb.append("Integrity & Factuality: ").append(valid ? "PASSED" : "FAILED").append("\n");
        sb.append("Status: ").append(status).append("\n");
        return sb.toString();
    }

    public static String formatConsensusInfo(String consensusId, String status, int epoch, boolean quorum) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KNOWLEDGE CONSENSUS DETAILS ===\n");
        sb.append("Consensus ID: ").append(consensusId).append("\n");
        sb.append("State: ").append(status).append("\n");
        sb.append("Epoch: ").append(epoch).append("\n");
        sb.append("Quorum Reached: ").append(quorum ? "YES" : "NO").append("\n");
        return sb.toString();
    }

    public static String formatSyncSummary(String runtimeId, boolean inSync, int missingCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KNOWLEDGE INVENTORY RECONCILIATION ===\n");
        sb.append("Runtime ID: ").append(runtimeId).append("\n");
        sb.append("Inventory State: ").append(inSync ? "IN SYNC" : "OUT OF SYNC").append("\n");
        sb.append("Missing Artifacts: ").append(missingCount).append("\n");
        return sb.toString();
    }
}
