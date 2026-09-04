package tech.kayys.wayang.knowledge.snapshot.packing;

public interface KnowledgeAnswerResolutionBlockCompressor {

    String algorithm();

    byte[] compress(byte[] input);

    byte[] decompress(byte[] input);
}
