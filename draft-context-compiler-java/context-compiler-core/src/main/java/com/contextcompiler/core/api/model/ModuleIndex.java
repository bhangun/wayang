package com.contextcompiler.core.api.model;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maps every top-level type declared in the repo to its file, and back.
 * The Java analogue of the Python original's ModuleIndex, adapted for the
 * fact that Java resolves types either via explicit import or by being in
 * the same package -- there is no wildcard-relative import ambiguity to
 * walk up through, so resolution here is a flat lookup, not a prefix walk.
 */
public final class ModuleIndex {

    private final Path root;
    private final Map<Path, String> pathToType = new LinkedHashMap<>();
    private final Map<String, Path> typeToPath = new LinkedHashMap<>();
    private final Map<String, Set<String>> packageMembers = new LinkedHashMap<>();

    public ModuleIndex(Path root) {
        this.root = root;
    }

    public Path root() {
        return root;
    }

    public Map<Path, String> pathToType() {
        return pathToType;
    }

    public Map<String, Path> typeToPath() {
        return typeToPath;
    }

    public Map<String, Set<String>> packageMembers() {
        return packageMembers;
    }

    public void register(Path file, String packageName, String simpleTypeName) {
        String fqn = packageName.isEmpty() ? simpleTypeName : packageName + "." + simpleTypeName;
        pathToType.put(file, fqn);
        typeToPath.put(fqn, file);
        packageMembers.computeIfAbsent(packageName, p -> new LinkedHashSet<>()).add(simpleTypeName);
    }

    /** Best-effort resolution of an imported or bare type name to a file in the repo. */
    public Optional<Path> resolveTypeName(String possiblyQualifiedName) {
        Path exact = typeToPath.get(possiblyQualifiedName);
        if (exact != null) {
            return Optional.of(exact);
        }
        int lastDot = possiblyQualifiedName.lastIndexOf('.');
        String simple = lastDot >= 0 ? possiblyQualifiedName.substring(lastDot + 1) : possiblyQualifiedName;
        for (Map.Entry<String, Path> entry : typeToPath.entrySet()) {
            String fqn = entry.getKey();
            if (fqn.equals(simple) || fqn.endsWith("." + simple)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /** Resolves a bare simple type name declared in the same package -- Java's import-free case. */
    public Optional<Path> resolveSamePackageType(String fromPackage, String simpleName) {
        Set<String> members = packageMembers.get(fromPackage);
        if (members != null && members.contains(simpleName)) {
            String fqn = fromPackage.isEmpty() ? simpleName : fromPackage + "." + simpleName;
            return Optional.ofNullable(typeToPath.get(fqn));
        }
        return Optional.empty();
    }
}
