package com.contextcompiler.core.api.model;

/**
 * The context strategy, now five levels instead of a strict binary tiering.
 * Every level below FULL_SOURCE only ever narrows *scope* (which members are
 * shown), never *fidelity* (how they're shown) -- nothing below this enum is
 * ever an AI-generated summary or paraphrase, only exact source substrings.
 *
 *   FULL_SOURCE        the file actively being edited
 *   SKELETON           reachable file, full interface: every member's
 *                       signature + Javadoc, bodies stripped
 *   SKELETON_PRUNED     reachable file, interface narrowed to only the
 *                       members confidently attributed as used along the
 *                       reachable chain (plus constructors/fields); omitted
 *                       members are disclosed with a count, not hidden
 *   SIGNATURE_DIGEST    reachable file that didn't earn a full skeleton under
 *                       budget pressure: bare signatures only, one line per
 *                       member, no Javadoc/annotations -- still exact text,
 *                       just the leanest non-empty representation
 *   EXCLUDED            everything else (never materialized as an entry)
 */
public enum Tier {
    FULL_SOURCE(1),
    SKELETON(2),
    SKELETON_PRUNED(3),
    SIGNATURE_DIGEST(4),
    EXCLUDED(5);

    private final int level;

    Tier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }
}
