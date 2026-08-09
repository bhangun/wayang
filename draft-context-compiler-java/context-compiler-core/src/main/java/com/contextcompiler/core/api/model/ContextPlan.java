package com.contextcompiler.core.api.model;

/**
 * @param maxHops   call-hop depth to resolve outward from the target
 * @param tokenBudget total budget to hand BudgetedContextCompiler
 * @param rationale human-readable explanation of the chosen numbers, so the
 *                  plan stays inspectable and adjustable rather than opaque
 */
public record ContextPlan(int maxHops, long tokenBudget, String rationale) {}
