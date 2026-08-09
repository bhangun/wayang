package com.contextcompiler.quarkus;

import com.contextcompiler.core.api.BudgetedContextCompiler;
import com.contextcompiler.core.api.ContextCompiler;
import com.contextcompiler.core.api.ContextPlanner;
import com.contextcompiler.core.api.RelevanceScorer;
import com.contextcompiler.core.api.Skeletonizer;
import com.contextcompiler.core.api.SymbolResolver;
import com.contextcompiler.core.api.TokenEstimator;
import com.contextcompiler.core.impl.DefaultBudgetedContextCompiler;
import com.contextcompiler.core.impl.DefaultContextCompiler;
import com.contextcompiler.core.impl.DefaultContextPlanner;
import com.contextcompiler.core.impl.DefaultRelevanceScorer;
import com.contextcompiler.core.impl.DefaultTokenEstimator;
import com.contextcompiler.core.impl.HeuristicSymbolResolver;
import com.contextcompiler.core.impl.JavaParserSkeletonizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Wires the abstraction layer to its default implementations as CDI beans.
 *
 * The default SymbolResolver is the stateless HeuristicSymbolResolver, safe
 * as an application-scoped singleton. TypeAwareSymbolResolver needs a
 * per-repo source root and is intentionally NOT produced here -- construct
 * one directly at the call site when a repo's source root is known and
 * context-compiler.type-aware-resolution=true.
 */
@ApplicationScoped
public class ContextCompilerProducer {

    @Produces
    @ApplicationScoped
    public TokenEstimator tokenEstimator() {
        return new DefaultTokenEstimator();
    }

    @Produces
    @ApplicationScoped
    public Skeletonizer skeletonizer() {
        return new JavaParserSkeletonizer();
    }

    @Produces
    @ApplicationScoped
    public SymbolResolver symbolResolver() {
        return new HeuristicSymbolResolver();
    }

    @Produces
    @ApplicationScoped
    public RelevanceScorer relevanceScorer() {
        return new DefaultRelevanceScorer();
    }

    @Produces
    @ApplicationScoped
    public ContextPlanner contextPlanner() {
        return new DefaultContextPlanner();
    }

    @Produces
    @ApplicationScoped
    public ContextCompiler contextCompiler(SymbolResolver symbolResolver, Skeletonizer skeletonizer,
                                            TokenEstimator tokenEstimator) {
        return new DefaultContextCompiler(symbolResolver, skeletonizer, tokenEstimator);
    }

    @Produces
    @ApplicationScoped
    public BudgetedContextCompiler budgetedContextCompiler(SymbolResolver symbolResolver, Skeletonizer skeletonizer,
                                                             TokenEstimator tokenEstimator,
                                                             RelevanceScorer relevanceScorer) {
        return new DefaultBudgetedContextCompiler(symbolResolver, skeletonizer, tokenEstimator, relevanceScorer);
    }
}
