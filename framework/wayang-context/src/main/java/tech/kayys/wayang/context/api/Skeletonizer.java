package tech.kayys.wayang.context.api;

import tech.kayys.wayang.context.api.exception.SkeletonizationException;
import tech.kayys.wayang.context.api.model.SkeletonResult;

import java.util.Set;

/**
 * Reduces a Java source file to increasingly narrow -- but always verbatim,
 * never AI-summarized -- views of its structure. Three levels, from richest
 * to leanest:
 *
 *   skeletonize        every member's signature + Javadoc, bodies stripped
 *   skeletonizePruned   only members named in a keep-set (plus constructors),
 *                        everything else omitted entirely, not just its body
 *   digest              bare signatures only, one line per member, no
 *                        Javadoc/annotations -- the leanest non-empty form
 *
 * Each level narrows *scope* (which members are shown), never *fidelity*
 * (how a shown member is written) -- every character that survives any of
 * these methods is a substring of the original source.
 */
public interface Skeletonizer {

    /**
     * @throws SkeletonizationException if {@code source} cannot be parsed as Java
     */
    SkeletonResult skeletonize(String source);

    /**
     * Like {@link #skeletonize}, but members not in {@code keepMemberNames}
     * are dropped entirely rather than just having their bodies stripped.
     * Constructors are always kept regardless of {@code keepMemberNames},
     * since a type without any visible way to construct it is rarely useful
     * context. Fields are always kept -- they're typically small and are
     * often the reason a caller understands a type's shape at all.
     *
     * @param keepMemberNames method names to preserve; an empty set keeps
     *                        constructors and fields only
     * @throws SkeletonizationException if {@code source} cannot be parsed as Java
     */
    SkeletonResult skeletonizePruned(String source, Set<String> keepMemberNames);

    /**
     * The leanest non-empty representation: one line per member, signature
     * only, no Javadoc, no annotations, no bodies. Intended as a last resort
     * under tight token budgets, not a default -- it tells a model a member
     * exists and its exact shape, nothing about why it exists.
     *
     * @throws SkeletonizationException if {@code source} cannot be parsed as Java
     */
    String digest(String source);
}
