package com.contextcompiler.core.impl;

import com.contextcompiler.core.api.ContextPlanner;
import com.contextcompiler.core.api.model.ContextPlan;
import com.contextcompiler.core.api.model.TaskIntent;

/**
 * Simple, explainable rules -- not a learned or LLM-driven planner. Intent
 * changes how far to look (maxHops) and how much of the window to spend
 * looking versus reserve for the model's own reasoning and output tokens.
 * Tune GENERATION_RESERVE and the per-intent numbers against your own models
 * and tasks; these are reasonable starting points, not measured optima.
 */
public final class DefaultContextPlanner implements ContextPlanner {

    private static final double GENERATION_RESERVE = 0.35;

    @Override
    public ContextPlan plan(TaskIntent intent, long modelContextWindowTokens) {
        long usable = Math.round(modelContextWindowTokens * (1 - GENERATION_RESERVE));

        return switch (intent) {
            case BUG_FIX -> new ContextPlan(2, usable,
                    "bug fix: shallow and precise -- the defect is usually within 1-2 call hops of the symptom");
            case REFACTOR -> new ContextPlan(3, usable,
                    "refactor: wider blast radius -- callers 2-3 hops out need to see the interface won't break");
            case NEW_FEATURE -> new ContextPlan(2, Math.round(usable * 0.8),
                    "new feature: moderate depth, budget held back for the new code being generated");
            case EXPLORATION -> new ContextPlan(4, usable,
                    "exploration: cast wide, coverage matters more than precision here");
        };
    }
}
