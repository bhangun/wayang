package tech.kayys.wayang.rag.model;

import tech.kayys.wayang.resource.Artifact;

/**
 * A part of a multimodal retrieval query.
 */
public sealed interface QueryPart {

    record TextPart(String text) implements QueryPart {}

    record ImagePart(Artifact imageArtifact) implements QueryPart {}

    record EmbeddingPart(java.util.List<Double> embedding) implements QueryPart {}
}
