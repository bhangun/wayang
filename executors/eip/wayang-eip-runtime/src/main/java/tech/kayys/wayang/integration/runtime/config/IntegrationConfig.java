package io.wayang.executors.integration.runtime.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "wayang.integration")
public interface IntegrationConfig {

    @WithName("engine.grpc.host")
    Optional<String> engineGrpcHost();

    @WithName("engine.grpc.port")
    Optional<Integer> engineGrpcPort();

    @WithName("executor.id")
    Optional<String> executorId();

    @WithName("enabled.modules")
    Optional<String> enabledModules();
}