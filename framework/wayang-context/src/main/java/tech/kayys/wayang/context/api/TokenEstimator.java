package tech.kayys.wayang.context.api;

/** Estimates token cost of a string. Swap in a real tokenizer (e.g. jtokkit) for exact counts. */
public interface TokenEstimator {
    long estimate(String text);
}
