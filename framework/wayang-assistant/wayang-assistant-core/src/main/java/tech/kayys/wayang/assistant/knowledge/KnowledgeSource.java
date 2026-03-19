package tech.kayys.wayang.assistant.knowledge;

/**
 * Represents a single knowledge source (documentation site or local docs folder).
 *
 * @param id          human-readable identifier used in search results
 * @param name        display name (e.g. "Wayang Docs")
 * @param baseUrl     the public URL of the docs site (for attribution links)
 * @param localCachePath  where fetched/cloned content is cached on disk
 * @param type        what kind of source this is
 */
public record KnowledgeSource(
        String id,
        String name,
        String baseUrl,
        String localCachePath,
        SourceType type
) {
    public enum SourceType {
        /** Local markdown files already on disk (e.g. wayang.github.io in the repo). */
        LOCAL_DOCS,
        /** A GitHub Pages site to be fetched via HTTP. */
        REMOTE_DOCS,
        /** A GitHub source code repository to be indexed for RAG. */
        GITHUB_REPO
    }

    /** Convenience factory for a remote GitHub Pages docs site. */
    public static KnowledgeSource remoteDocsSource(String id, String name, String baseUrl, String cacheDir) {
        return new KnowledgeSource(id, name, baseUrl, cacheDir, SourceType.REMOTE_DOCS);
    }

    /** Convenience factory for a GitHub source repo. */
    public static KnowledgeSource githubRepoSource(String id, String name, String repoUrl, String cacheDir) {
        return new KnowledgeSource(id, name, repoUrl, cacheDir, SourceType.GITHUB_REPO);
    }
}
