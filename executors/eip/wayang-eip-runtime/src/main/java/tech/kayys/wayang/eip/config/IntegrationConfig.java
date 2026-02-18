package tech.kayys.wayang.eip.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "wayang.integration")
public interface IntegrationConfig {

    @WithName("enabled.modules")
    @WithDefault("http,kafka,file")
    String enabledModules();

    @WithName("auto.deploy.enabled")
    @WithDefault("true")
    boolean autoDeployEnabled();
}
