package com.contextcompiler.core.api;

import com.contextcompiler.core.api.model.ContextPlan;
import com.contextcompiler.core.api.model.TaskIntent;

/**
 * Maps a task's intent to a concrete compilation plan: how many call hops to
 * resolve outward, and how much of the model's usable context window to
 * spend on retrieved context versus leave free for reasoning and generation.
 *
 * This is deliberately the only piece adopted from a much larger proposed
 * "context compilation pipeline" (Planner / Collector / Resolver / Filter /
 * Reducer / Ranker / Budgeter / Assembler / Validator, generalized across
 * code, tickets, and security data). Everything else in that proposal either
 * already exists here in a narrower, code-specific form (SymbolResolver,
 * Skeletonizer, BudgetedContextCompiler cover Resolver+Filter+Reducer+
 * Ranker+Budgeter+Assembler for one domain), or depends on a second real
 * domain existing before a shared abstraction is worth extracting. The
 * planner is the exception: it's genuinely new, doesn't require the
 * multi-domain Collector/Resolver generalization to be useful, and directly
 * answers a real question -- a bug-fix task and a refactor task warrant
 * different hop depth and budget allocation even for the same target file.
 */
public interface ContextPlanner {
    ContextPlan plan(TaskIntent intent, long modelContextWindowTokens);
}
