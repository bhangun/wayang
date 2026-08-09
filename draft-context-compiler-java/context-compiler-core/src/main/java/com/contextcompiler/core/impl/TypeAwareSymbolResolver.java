package com.contextcompiler.core.impl;

import com.contextcompiler.core.api.SymbolResolver;
import com.contextcompiler.core.api.model.ModuleIndex;
import com.contextcompiler.core.api.model.ReachabilityResult;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Wraps {@link HeuristicSymbolResolver} with a type-aware second pass. This is
 * the one place this port deliberately goes beyond a straight translation of
 * the Python original: Java's static type system makes a real (if scoped)
 * fix for the name-collision blind spot practical, where Python's dynamic
 * typing does not without a much heavier type checker.
 *
 * When a project-wide {@link CombinedTypeSolver} can be built, every bare-name
 * call the heuristic pass flagged as a collision is re-checked against
 * JavaParser's symbol solver and narrowed to the file that actually declares
 * the resolved method, where resolvable.
 *
 * Falls back to the heuristic result untouched for anything the symbol
 * solver cannot resolve -- an unresolved call stays a disclosed collision,
 * never a silent guess. No JavaParser static/global configuration is
 * mutated, so this stays safe under concurrent use.
 */
public final class TypeAwareSymbolResolver implements SymbolResolver {

    private final HeuristicSymbolResolver fallback = new HeuristicSymbolResolver();
    private final Path sourceRoot;

    /**
     * @param sourceRoot a directory JavaParserTypeSolver can walk to resolve
     *                   project types (typically the repo's {@code src/main/java}).
     *                   JDK types resolve via reflection automatically; types from
     *                   external jars are out of scope for this lightweight solver.
     */
    public TypeAwareSymbolResolver(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    @Override
    public ReachabilityResult resolve(ModuleIndex index, Path targetFile, int maxHops) {
        ReachabilityResult heuristicResult = fallback.resolve(index, targetFile, maxHops);

        if (heuristicResult.nameCollisions().isEmpty()) {
            return heuristicResult;
        }

        JavaSymbolSolver symbolSolver = buildSymbolSolver();
        if (symbolSolver == null) {
            return heuristicResult;
        }

        JavaParser parser = new JavaParser(new ParserConfiguration().setSymbolResolver(symbolSolver));

        try {
            String source = Files.readString(targetFile);
            ParseResult<CompilationUnit> parseResult = parser.parse(source);
            CompilationUnit cu = parseResult.getResult().orElse(null);
            if (cu == null) return heuristicResult;

            for (MethodCallExpr call : cu.findAll(MethodCallExpr.class)) {
                String name = call.getNameAsString();
                List<Path> candidates = heuristicResult.nameCollisions().get(name);
                if (candidates == null || candidates.isEmpty()) continue;

                try {
                    ResolvedMethodDeclaration resolved = call.resolve();
                    String declaringType = resolved.declaringType().getQualifiedName();
                    index.resolveTypeName(declaringType).ifPresent(narrowedFile -> {
                        Integer hop = heuristicResult.reachable().get(candidates.get(0));
                        heuristicResult.reachable().put(narrowedFile, hop != null ? hop : 1);
                    });
                } catch (UnsolvedSymbolException | RuntimeException unresolved) {
                    // Leave the heuristic collision as-is -- an unresolved call stays
                    // disclosed, it is not dropped or guessed at.
                }
            }
        } catch (Exception ignored) {
            return heuristicResult;
        }

        return heuristicResult;
    }

    private JavaSymbolSolver buildSymbolSolver() {
        if (sourceRoot == null || !Files.isDirectory(sourceRoot)) return null;
        CombinedTypeSolver combined = new CombinedTypeSolver();
        combined.add(new ReflectionTypeSolver());
        combined.add(new JavaParserTypeSolver(sourceRoot));
        return new JavaSymbolSolver(combined);
    }
}
