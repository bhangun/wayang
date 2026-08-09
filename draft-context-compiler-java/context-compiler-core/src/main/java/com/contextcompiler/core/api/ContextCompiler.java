package com.contextcompiler.core.api;

import com.contextcompiler.core.api.model.CompiledContext;

import java.nio.file.Path;

/**
 * Orchestrates the three-tier context strategy:
 *   Tier 1  Full source            the file actively being edited
 *   Tier 2  Skeletonized interface  files reachable within maxHops call hops
 *   Tier 3  Total exclusion         everything else in the repo
 */
public interface ContextCompiler {
    CompiledContext compile(Path repoRoot, Path targetFile, int maxHops);
}
