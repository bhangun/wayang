package com.contextcompiler.quarkus.rest;

import com.contextcompiler.core.api.BudgetedContextCompiler;
import com.contextcompiler.core.api.ContextCompiler;
import com.contextcompiler.core.api.ContextPlanner;
import com.contextcompiler.core.api.model.CompiledContext;
import com.contextcompiler.core.api.model.ContextPlan;
import com.contextcompiler.core.api.model.TaskIntent;
import com.contextcompiler.quarkus.ContextCompilerConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Reactive endpoint over ContextCompiler / BudgetedContextCompiler. compile()
 * offloads the (blocking, filesystem-heavy) compile call to the worker pool,
 * matching this codebase's "reactive at the edges, blocking work off the
 * event loop" convention.
 */
@Path("/api/context-compiler")
public class ContextCompilerResource {

    @Inject
    ContextCompiler contextCompiler;

    @Inject
    BudgetedContextCompiler budgetedContextCompiler;

    @Inject
    ContextPlanner contextPlanner;

    @Inject
    ContextCompilerConfig config;

    @POST
    @Path("/compile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<CompiledContextDto> compile(CompileRequest request) {
        int maxHops = request.maxHops() != null ? request.maxHops() : config.defaultMaxHops();
        return Uni.createFrom()
                .item(() -> contextCompiler.compile(
                        java.nio.file.Path.of(request.repoRoot()),
                        java.nio.file.Path.of(request.targetFile()),
                        maxHops))
                .map(CompiledContextDto::from)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    /**
     * Planner-driven, budget-aware compile -- the path intended for a small
     * or open-weight model. intent picks hop depth and budget allocation;
     * contextWindowTokens defaults to context-compiler.default-model-context-window-tokens
     * (an 8K local model, out of the box).
     */
    @POST
    @Path("/compile/planned")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<CompiledContextDto> compilePlanned(PlannedCompileRequest request) {
        long contextWindow = request.contextWindowTokens() != null
                ? request.contextWindowTokens()
                : config.defaultModelContextWindowTokens();
        TaskIntent intent = request.intent() != null ? request.intent() : TaskIntent.EXPLORATION;

        return Uni.createFrom()
                .item(() -> {
                    ContextPlan plan = contextPlanner.plan(intent, contextWindow);
                    return budgetedContextCompiler.compile(
                            java.nio.file.Path.of(request.repoRoot()),
                            java.nio.file.Path.of(request.targetFile()),
                            plan.maxHops(),
                            plan.tokenBudget());
                })
                .map(CompiledContextDto::from)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/compile/prompt")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> compilePromptString(@QueryParam("repoRoot") String repoRoot,
                                            @QueryParam("targetFile") String targetFile,
                                            @QueryParam("maxHops") @DefaultValue("2") int maxHops) {
        return Uni.createFrom()
                .item(() -> contextCompiler.compile(
                        java.nio.file.Path.of(repoRoot),
                        java.nio.file.Path.of(targetFile),
                        maxHops))
                .map(CompiledContext::toPromptString)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
