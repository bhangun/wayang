package com.contextcompiler.quarkus.rest;

public record CompileRequest(String repoRoot, String targetFile, Integer maxHops) {}
