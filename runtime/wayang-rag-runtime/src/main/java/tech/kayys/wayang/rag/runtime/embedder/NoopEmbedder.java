package tech.kayys.wayang.rag.runtime.embedder;

import tech.kayys.wayang.rag.spi.RagEmbedder;

import java.util.List;

/**
 * Placeholder embedder that returns zero-vectors.
 * Replace with a real embedding model (e.g. via wayang-embedding) in production.
 */
public class NoopEmbedder implements RagEmbedder {

    private final int dims;

    public NoopEmbedder() { this(384); }
    public NoopEmbedder(int dims) { this.dims = dims; }

    @Override
    public float[] embed(String text) { return new float[dims]; }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        return texts.stream().map(t -> new float[dims]).toList();
    }

    @Override
    public int dimensions() { return dims; }
}
