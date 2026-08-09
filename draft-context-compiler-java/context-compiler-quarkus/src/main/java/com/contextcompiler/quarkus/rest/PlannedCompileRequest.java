package com.contextcompiler.quarkus.rest;

import com.contextcompiler.core.api.model.TaskIntent;

/**
 * @param intent              defaults to EXPLORATION if omitted
 * @param contextWindowTokens defaults to context-compiler.default-model-context-window-tokens if omitted
 */
public record PlannedCompileRequest(String repoRoot, String targetFile, TaskIntent intent, Long contextWindowTokens) {}
