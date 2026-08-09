package tech.kayys.wayang.sandbox.docker;

import tech.kayys.wayang.spi.sandbox.Sandbox;
import tech.kayys.wayang.spi.sandbox.SandboxExecutionResult;
import tech.kayys.wayang.spi.sandbox.SandboxConfiguration;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class DockerSandbox implements Sandbox {

    private final DockerClient dockerClient;
    private final SandboxConfiguration config;
    private String containerId;

    public DockerSandbox(DockerClient dockerClient, SandboxConfiguration config) {
        this.dockerClient = dockerClient;
        this.config = config;
    }

    @Override
    public void start() throws Exception {
        String image = config.getImage() != null ? config.getImage() : "ubuntu:latest";
        
        // Ensure image exists or pull it (simplified for this example)
        
        HostConfig hostConfig = HostConfig.newHostConfig();
        if (config.getWorkingDirectory() != null) {
            Volume volume = new Volume("/workspace");
            hostConfig.withBinds(new Bind(config.getWorkingDirectory(), volume));
        }

        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withCmd("tail", "-f", "/dev/null") // Keep alive
                .withHostConfig(hostConfig)
                .withWorkingDir(config.getWorkingDirectory() != null ? "/workspace" : "/")
                .exec();

        this.containerId = container.getId();
        dockerClient.startContainerCmd(containerId).exec();
    }

    @Override
    public void stop() throws Exception {
        if (containerId != null) {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            containerId = null;
        }
    }

    @Override
    public SandboxExecutionResult executeCommand(String command, long timeoutMillis) throws Exception {
        if (containerId == null) throw new IllegalStateException("Sandbox not started");

        ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                .withCmd("bash", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        ExecStartResultCallback callback = new ExecStartResultCallback(stdout, stderr);
        dockerClient.execStartCmd(execCreate.getId()).exec(callback);
        
        boolean completed = callback.awaitCompletion(timeoutMillis, TimeUnit.MILLISECONDS);
        if (!completed) {
            return new SandboxExecutionResult(-1, stdout.toString(), "Timeout: " + stderr.toString());
        }

        int exitCode = dockerClient.execInspectCmd(execCreate.getId()).exec().getExitCodeLong().intValue();
        return new SandboxExecutionResult(exitCode, stdout.toString(), stderr.toString());
    }

    @Override
    public void writeFile(String path, String content) throws Exception {
        // Since the workspace is mapped via volume mount, we can just write it to the host path.
        if (config.getWorkingDirectory() != null) {
            Files.writeString(Paths.get(config.getWorkingDirectory(), path), content);
        } else {
            throw new UnsupportedOperationException("WriteFile requires working directory mapping");
        }
    }

    @Override
    public String readFile(String path) throws Exception {
        if (config.getWorkingDirectory() != null) {
            return Files.readString(Paths.get(config.getWorkingDirectory(), path));
        } else {
            throw new UnsupportedOperationException("ReadFile requires working directory mapping");
        }
    }
}
