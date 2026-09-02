package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class DefaultKnowledgeEvidenceMerkleTreeBuilder
        implements KnowledgeEvidenceMerkleTreeBuilder {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public KnowledgeEvidenceMerkleTree build(List<KnowledgeEvidenceMerkleLeaf> inputLeaves) {
        if (inputLeaves == null || inputLeaves.isEmpty()) {
            throw new IllegalArgumentException("At least one Merkle leaf is required");
        }

        List<KnowledgeEvidenceMerkleLeaf> leaves = new ArrayList<>(inputLeaves);
        leaves.sort(Comparator.comparing(KnowledgeEvidenceMerkleLeaf::leafId));

        Map<String, KnowledgeEvidenceMerkleNode> nodes = new LinkedHashMap<>();
        List<String> current = new ArrayList<>();

        for (KnowledgeEvidenceMerkleLeaf leaf : leaves) {
            String hash = leafHash(leaf);
            nodes.put(hash, new KnowledgeEvidenceMerkleNode(
                    hash, null, null, true, leaf.artifactId(), leaf.metadata()
            ));
            current.add(hash);
        }

        while (current.size() > 1) {
            List<String> next = new ArrayList<>();
            for (int i = 0; i < current.size(); i += 2) {
                String left = current.get(i);
                String right = (i + 1 < current.size()) ? current.get(i + 1) : left;
                String parent = hashPair(left, right);

                nodes.put(parent, new KnowledgeEvidenceMerkleNode(
                        parent, left, right, false, null, Map.of()
                ));
                next.add(parent);
            }
            current = next;
        }

        return new KnowledgeEvidenceMerkleTree(
                "SHA-256",
                current.getFirst(),
                leaves,
                nodes,
                Map.of()
        );
    }

    private String leafHash(KnowledgeEvidenceMerkleLeaf leaf) {
        return sha256("leaf|" + leaf.leafId() + "|" + leaf.artifactId() + "|" +
                leaf.resourceType() + "|" + leaf.contentHash() + "|" + leaf.size());
    }

    private String hashPair(String left, String right) {
        return sha256("node|" + left + "|" + right);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate Merkle hash", e);
        }
    }
}
