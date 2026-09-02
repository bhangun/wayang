package tech.kayys.wayang.knowledge.snapshot.merkle;

public record KnowledgeEvidenceMerkleProofStep(
        String siblingHash,
        Direction direction
) {
    public enum Direction {
        LEFT,
        RIGHT
    }
}
