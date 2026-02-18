/*
 * PolyForm Noncommercial License 1.0.0
 *
 * Copyright (c) 2026 Kayys.tech
 *
 * This software is licensed for non-commercial use only.
 * You may use, modify, and distribute this software for personal,
 * educational, or research purposes.
 *
 * Commercial use, including SaaS or revenue-generating services,
 * requires a separate commercial license from Kayys.tech.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 *
 * @author Bhangun
 */

package tech.kayys.wayang.plugin.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.plugin.execution.ExecutionMode;

/**
 * Executor manifest
 */
public class ExecutorManifest {
    public String executorId;
    public String className;
    public List<String> nodeTypes = new ArrayList<>();
    public ExecutionMode mode;
    public List<String> protocols = new ArrayList<>();
    public boolean inProcess;
    public Map<String, Object> config = new HashMap<>();
}
