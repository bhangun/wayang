package tech.kayys.wayang.spi.sandbox;

public record SandboxExecutionResult(int exitCode, String stdout, String stderr) {
    public boolean isSuccess() {
        return exitCode == 0;
    }
}
