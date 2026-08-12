package tech.kayys.wayang.cache;

import tech.kayys.wayang.project.ProjectContext;

/**
 * A strongly-typed key for caching derived or retrieved content.
 * It incorporates content/version fingerprints to safely deduplicate execution steps.
 */
public record CacheKey(
    ProjectContext project,
    String artifactId,
    String artifactVersion,
    String operation,
    String modelVersion,
    String promptVersion
) {
    /**
     * Generates a deterministic hash for this cache key.
     */
    public String hash() {
        return String.format("%s:%s:%s:%s:%s:%s",
            project != null ? project.projectId() : "global",
            artifactId,
            artifactVersion,
            operation,
            modelVersion,
            promptVersion
        );
    }
}
