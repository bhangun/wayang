package tech.kayys.wayang.sandbox.docker;

import tech.kayys.wayang.spi.sandbox.Sandbox;
import tech.kayys.wayang.spi.sandbox.SandboxConfiguration;
import tech.kayys.wayang.spi.sandbox.SandboxProvider;
import jakarta.enterprise.context.ApplicationScoped;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DefaultDockerClientConfig;

@ApplicationScoped
public class DockerSandboxProvider implements SandboxProvider {

    @Override
    public String getProviderId() {
        return "docker";
    }

    @Override
    public Sandbox createSandbox(SandboxConfiguration config) throws Exception {
        DefaultDockerClientConfig dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(dockerConfig).build();
        return new DockerSandbox(dockerClient, config);
    }
}
