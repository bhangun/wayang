package tech.kayys.wayang.spi.sandbox;

public interface Sandbox extends AutoCloseable {
    
    /**
     * Initializes the sandbox environment.
     */
    void start() throws Exception;
    
    /**
     * Stops and cleans up the sandbox environment.
     */
    void stop() throws Exception;
    
    @Override
    default void close() throws Exception {
        stop();
    }
    
    /**
     * Executes a command inside the sandbox.
     * @param command the command to execute (e.g., "mvn test")
     * @param timeoutMillis maximum time to wait for completion
     * @return result containing exit code and output
     */
    SandboxExecutionResult executeCommand(String command, long timeoutMillis) throws Exception;
    
    /**
     * Writes content to a file inside the sandbox.
     * @param path absolute or relative path within the sandbox
     * @param content file content
     */
    void writeFile(String path, String content) throws Exception;
    
    /**
     * Reads content from a file inside the sandbox.
     * @param path absolute or relative path within the sandbox
     * @return file content
     */
    String readFile(String path) throws Exception;
}
