package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.SymbolResolver;
import tech.kayys.wayang.context.api.model.ModuleIndex;
import tech.kayys.wayang.context.api.model.ReachabilityResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Import- and name-based reachability, in the spirit of the Python original:
 * resolve what an explicit import or same-package reference explains, fall
 * back to a repo-wide bare-name method table for the rest, and flag anything
 * the fallback cannot say with confidence rather than guessing silently.
 *
 * Three blind spots follow directly from that trade-off, mirroring the three
 * documented in the Python resolver:
 *   1. Reflection-based dispatch (Class.forName / Method.invoke) is invisible
 *      to static analysis -- flagged, not resolved.
 *   2. Event-style annotations (@Observes, @ConsumeEvent, @Incoming, ...) wire
 *      a method at runtime with no direct call site -- flagged as a hint.
 *   3. Bare method-name resolution means unrelated types sharing a method
 *      name (e.g. two repositories both declaring save()) can both be pulled
 *      into tier 2 -- reported as a name collision, never silently dropped.
 *
 * Stateless by design: no per-repo cache lives on {@code this}, so a single
 * instance is safe to hold as an application-scoped CDI singleton serving
 * concurrent requests against different repos.
 */
public final class HeuristicSymbolResolver implements SymbolResolver {

    private static final Set<String> REFLECTION_CALL_NAMES = Set.of(
            "forName", "getMethod", "getDeclaredMethod", "invoke", "newInstance"
    );

    // The Java/Quarkus-reactive analogue of Python's @receiver-style signals:
    // annotation-driven wiring with no direct AST edge from caller to handler.
    private static final Set<String> KNOWN_EVENT_ANNOTATIONS = Set.of(
            "Observes", "ObservesAsync", "ConsumeEvent", "Incoming", "Outgoing", "Scheduled"
    );

    @Override
    public ReachabilityResult resolve(ModuleIndex index, Path targetFile, int maxHops) {
        Map<Path, FileSymbols> symbolCache = new ConcurrentHashMap<>();
        ReachabilityResult result = new ReachabilityResult();
        Map<String, List<Path>> methodTable = buildMethodTable(index, symbolCache);
        Set<String> allCalledNamesSeen = new LinkedHashSet<>();

        Set<Path> visited = new HashSet<>();
        visited.add(targetFile);
        List<Path> currentLayer = List.of(targetFile);

        int hop = 0;
        while (!currentLayer.isEmpty() && hop < maxHops) {
            hop++;
            List<Path> nextLayer = new ArrayList<>();

            for (Path file : currentLayer) {
                FileSymbols symbols = symbolFor(file, symbolCache);
                if (symbols == null) continue;

                allCalledNamesSeen.addAll(symbols.calledMethodNames);

                if (symbols.usesReflection) {
                    result.dynamicDispatchFiles().add(file);
                }
                Set<String> eventHints = new LinkedHashSet<>(symbols.annotationNames);
                eventHints.retainAll(KNOWN_EVENT_ANNOTATIONS);
                if (!eventHints.isEmpty()) {
                    result.eventAnnotationHints().put(file, eventHints);
                }

                // (a) explicit imports + same-package type references -- no
                // precise call-site attribution here, only "this file is
                // referenced"; member attribution for these is filled in by
                // the conservative cross-reference pass below.
                final int currentHop = hop;
                for (String typeRef : symbols.referencedTypeNames) {
                    index.resolveTypeName(typeRef)
                            .or(() -> index.resolveSamePackageType(symbols.packageName, typeRef))
                            .ifPresent(dep -> {
                                bumpCallSite(result, dep);
                                addIfNew(dep, currentHop, visited, result, nextLayer);
                            });
                }

                // (b) bare method-name fallback -- the source of the documented
                // name-collision blind spot, same role as Python's `.save()`
                // case. Precise here: calledName IS the member that pulled
                // candidate in, so it's recorded directly in usedMembers.
                for (String calledName : symbols.calledMethodNames) {
                    List<Path> candidates = methodTable.get(calledName);
                    if (candidates == null || candidates.isEmpty()) continue;
                    if (candidates.size() > 1) {
                        result.nameCollisions().put(calledName, candidates);
                    }
                    for (Path candidate : candidates) {
                        result.usedMembers()
                                .computeIfAbsent(candidate, k -> new LinkedHashSet<>())
                                .add(calledName);
                        bumpCallSite(result, candidate);
                        addIfNew(candidate, hop, visited, result, nextLayer);
                    }
                }
            }
            currentLayer = nextLayer;
        }

        result.reachable().remove(targetFile);

        // Conservative cross-reference pass: a file reached only through a
        // type reference (no direct call-site match) has no attribution yet.
        // Credit it with any of its own member names that were also called
        // *somewhere* in the traversal -- over-inclusive by design, same
        // direction of error the resolver already accepts elsewhere. Files
        // with confirmed attribution from (b) are left untouched.
        for (Path file : result.reachable().keySet()) {
            if (!result.usedMembers().getOrDefault(file, Set.of()).isEmpty()) continue;
            FileSymbols fileSymbols = symbolFor(file, symbolCache);
            if (fileSymbols == null) continue;
            Set<String> plausible = new LinkedHashSet<>(fileSymbols.definedMethodNames);
            plausible.retainAll(allCalledNamesSeen);
            if (!plausible.isEmpty()) {
                result.usedMembers().put(file, plausible);
            }
        }

        return result;
    }

    private void addIfNew(Path dep, int hop, Set<Path> visited, ReachabilityResult result, List<Path> nextLayer) {
        if (visited.add(dep)) {
            result.reachable().put(dep, hop);
            nextLayer.add(dep);
        }
    }

    private void bumpCallSite(ReachabilityResult result, Path file) {
        result.callSiteCounts().merge(file, 1, Integer::sum);
    }

    private Map<String, List<Path>> buildMethodTable(ModuleIndex index, Map<Path, FileSymbols> symbolCache) {
        Map<String, List<Path>> table = new LinkedHashMap<>();
        for (Path file : index.pathToType().keySet()) {
            FileSymbols symbols = symbolFor(file, symbolCache);
            if (symbols == null) continue;
            for (String methodName : symbols.definedMethodNames) {
                table.computeIfAbsent(methodName, n -> new ArrayList<>()).add(file);
            }
        }
        return table;
    }

    private FileSymbols symbolFor(Path file, Map<Path, FileSymbols> cache) {
        return cache.computeIfAbsent(file, this::parseSymbols);
    }

    private FileSymbols parseSymbols(Path file) {
        try {
            String source = Files.readString(file);
            CompilationUnit cu = StaticJavaParser.parse(source);
            FileSymbols symbols = new FileSymbols();
            symbols.packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
            cu.getImports().forEach(imp -> symbols.referencedTypeNames.add(imp.getNameAsString()));

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceType n, Void arg) {
                    symbols.referencedTypeNames.add(n.getNameAsString());
                    super.visit(n, arg);
                }

                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    symbols.calledMethodNames.add(n.getNameAsString());
                    if (REFLECTION_CALL_NAMES.contains(n.getNameAsString())) {
                        symbols.usesReflection = true;
                    }
                    super.visit(n, arg);
                }

                @Override
                public void visit(MethodDeclaration n, Void arg) {
                    symbols.definedMethodNames.add(n.getNameAsString());
                    n.getAnnotations().forEach(a -> symbols.annotationNames.add(a.getNameAsString()));
                    super.visit(n, arg);
                }

                @Override
                public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                    n.getAnnotations().forEach(a -> symbols.annotationNames.add(a.getNameAsString()));
                    super.visit(n, arg);
                }
            }, null);

            return symbols;
        } catch (Exception parseFailure) {
            return null; // unreadable/unparsable files are excluded, not guessed at
        }
    }

    /** Per-file symbol summary, scoped to a single resolve() call via a local cache. */
    private static final class FileSymbols {
        String packageName = "";
        final Set<String> referencedTypeNames = new LinkedHashSet<>();
        final Set<String> calledMethodNames = new LinkedHashSet<>();
        final Set<String> definedMethodNames = new LinkedHashSet<>();
        final Set<String> annotationNames = new LinkedHashSet<>();
        boolean usesReflection = false;
    }
}
