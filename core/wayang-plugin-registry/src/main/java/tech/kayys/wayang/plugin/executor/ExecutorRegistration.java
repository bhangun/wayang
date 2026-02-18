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

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import tech.kayys.wayang.plugin.CommunicationProtocol;

/**
 * Executor Registration - What Control Plane knows about an executor
 */
public class ExecutorRegistration {
    public String executorId;
    public String executorType; // "agent", "integration", "business"
    public URI endpoint;
    public CommunicationProtocol protocol;
    public Set<String> capabilities = new HashSet<>();
    public Set<String> supportedNodes = new HashSet<>();
    public ExecutorStatus status = ExecutorStatus.PENDING;
    public ExecutorMetadata metadata = new ExecutorMetadata();
    public Instant registeredAt;
    public Instant lastHeartbeat;

    // In-process plugin executor support
    public boolean inProcess = false;
    public Object executorInstance;
}