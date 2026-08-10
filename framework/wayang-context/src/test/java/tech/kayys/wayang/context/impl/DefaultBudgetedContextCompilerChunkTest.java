package tech.kayys.wayang.context.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.kayys.wayang.context.api.model.CompiledContext;
import tech.kayys.wayang.context.api.model.Tier;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultBudgetedContextCompilerChunkTest {

    @TempDir
    Path repo;

    @Test
    void compilesLargeTargetAsLineAddressableChunks() throws Exception {
        Path sourceRoot = repo.resolve("src/main/java/demo");
        Files.createDirectories(sourceRoot);
        Path target = sourceRoot.resolve("LargeTarget.java");
        Files.writeString(target, largeJavaClass());

        DefaultBudgetedContextCompiler compiler = new DefaultBudgetedContextCompiler(
                new HeuristicSymbolResolver(),
                new JavaParserSkeletonizer(),
                new DefaultTokenEstimator(),
                new DefaultRelevanceScorer(),
                new SourceCodeChunker(new LineWindowSourceReader(200, 20))
        );

        CompiledContext context = compiler.compile(repo, target, 1, 2_200);

        assertTrue(context.entries().stream().anyMatch(entry -> entry.tier() == Tier.SOURCE_CHUNK));
        assertTrue(context.entries().stream().noneMatch(entry -> entry.tier() == Tier.FULL_SOURCE));
        assertTrue(context.toPromptString().contains("source lines 1-200"));
        assertTrue(context.compiledTokens() <= 2_200);
    }

    private String largeJavaClass() {
        StringBuilder source = new StringBuilder();
        source.append("package demo;\n\n");
        source.append("public class LargeTarget {\n");
        source.append("    public int value() {\n");
        source.append("        int total = 0;\n");
        for (int i = 0; i < 450; i++) {
            source.append("        total += ").append(i).append(";\n");
        }
        source.append("        return total;\n");
        source.append("    }\n");
        source.append("}\n");
        return source.toString();
    }
}
