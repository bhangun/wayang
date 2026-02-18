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

import java.util.HashMap;
import java.util.Map;

import tech.kayys.wayang.plugin.CommunicationProtocol;
import tech.kayys.wayang.plugin.execution.ExecutionMode;

/**
 * Executor Binding - Indirect reference to executor
 * This allows remote, multi-language, and hot-swappable executors
 */
public class ExecutorBinding {
    public String executorType; // "agent", "integration", "business", "custom"
    public String executorId; // "agent.default", "http.rest", "vector.pgvector"
    public ExecutionMode mode; // SYNC | ASYNC | STREAM
    public CommunicationProtocol protocol; // GRPC | KAFKA | REST | INPROC
    public Map<String, Object> config = new HashMap<>();

    public ExecutorBinding() {
    }

    public ExecutorBinding(String executorId, ExecutionMode mode, CommunicationProtocol protocol) {
        this.executorId = executorId;
        this.mode = mode;
        this.protocol = protocol;
    }
}
