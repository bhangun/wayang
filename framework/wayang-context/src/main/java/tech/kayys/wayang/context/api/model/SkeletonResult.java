package tech.kayys.wayang.context.api.model;

/** Output of a single skeletonization pass over one file's source. */
public record SkeletonResult(String skeleton, int membersStripped) {}
