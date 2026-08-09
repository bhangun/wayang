package com.contextcompiler.core.api;

import com.contextcompiler.core.api.model.ModuleIndex;
import com.contextcompiler.core.api.model.ReachabilityResult;

import java.nio.file.Path;

/**
 * Builds an approximate reachability graph outward from a target file, out to
 * a configurable call-hop depth. Implementations trade soundness for speed
 * and must surface blind spots as diagnostics rather than hide them.
 *
 * Implementations must be safe to call concurrently and safe to hold as an
 * application-scoped singleton -- no per-repo state may live on {@code this}.
 */
public interface SymbolResolver {
    ReachabilityResult resolve(ModuleIndex index, Path targetFile, int maxHops);
}
