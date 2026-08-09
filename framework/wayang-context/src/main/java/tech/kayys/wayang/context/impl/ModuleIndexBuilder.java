package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.model.ModuleIndex;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Scans a repository once and maps every top-level type to its file, and
 * back. Rebuilt fresh on every call -- same "no caching across runs" trade-off
 * the Python original documents; callers that compile against the same repo
 * repeatedly should cache the returned ModuleIndex themselves (e.g. keyed by
 * repo root, invalidated on a directory watch or a short TTL).
 */
public final class ModuleIndexBuilder {

    private static final Set<String> SKIP_DIRS = Set.of(
            ".git", "target", "build", "node_modules", ".idea", "out"
    );

    private ModuleIndexBuilder() {
    }

    public static ModuleIndex build(Path root) {
        ModuleIndex index = new ModuleIndex(root);
        String sep = root.getFileSystem().getSeparator();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                 .filter(p -> SKIP_DIRS.stream().noneMatch(skip -> p.toString().contains(sep + skip + sep)))
                 .forEach(file -> indexFile(index, file));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to scan repository at " + root, e);
        }
        return index;
    }

    private static void indexFile(ModuleIndex index, Path file) {
        try {
            String source = Files.readString(file);
            CompilationUnit cu = StaticJavaParser.parse(source);
            String packageName = cu.getPackageDeclaration()
                    .map(pd -> pd.getNameAsString())
                    .orElse("");
            for (TypeDeclaration<?> type : cu.getTypes()) {
                index.register(file, packageName, type.getNameAsString());
            }
        } catch (Exception parseFailure) {
            // A file that fails to parse never becomes reachable -- it is
            // excluded, not silently guessed at, matching the resolver's stance.
        }
    }
}
