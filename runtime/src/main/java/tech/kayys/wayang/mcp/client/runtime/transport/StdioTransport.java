package tech.kayys.wayang.mcp.client.runtime.transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import reactor.core.publisher.Mono;

/**
 * STDIO Transport Implementation (for process-based servers)
 */
public class StdioTransport extends MCPTransport {
    
    private final String[] command;
    private Process process;
    private BufferedWriter processInput;
    private BufferedReader processOutput;
    private Thread outputReaderThread;
    
    public StdioTransport(String[] command) {
        this.command = command;
    }
    
    @Override
    public Mono<Void> connect() {
        if (shuttingDown) {
            return Mono.error(new RuntimeException("Transport is shutting down"));
        }
        
        return Mono.fromRunnable(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                process = pb.start();
                
                processInput = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
                );
                processOutput = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
                );
                
                // Start output reader thread
                outputReaderThread = new Thread(this::readOutput);
                outputReaderThread.setDaemon(true);
                outputReaderThread.start();
                
                connected = true;
                logger.info("Started MCP server process: {}", String.join(" ", command));
                
            } catch (Exception e) {
                logger.error("Failed to start MCP server process: {}", String.join(" ", command), e);
                throw new RuntimeException("Failed to start MCP server process", e);
            }
        });
    }
    
    @Override
    public Mono<Void> disconnect() {
        return Mono.fromRunnable(() -> {
            connected = false;
            shuttingDown = true;
            
            if (outputReaderThread != null) {
                outputReaderThread.interrupt();
            }
            
            if (processInput != null) {
                try {
                    processInput.close();
                } catch (IOException e) {
                    logger.error("Error closing process input", e);
                }
            }
            
            if (processOutput != null) {
                try {
                    processOutput.close();
                } catch (IOException e) {
                    logger.error("Error closing process output", e);
                }
            }
            
            if (process != null) {
                process.destroyForcibly();
                logger.info("MCP server process terminated: {}", String.join(" ", command));
            }
        });
    }
    
    @Override
    public void sendMessage(String message) {
        if (shuttingDown) {
            throw new RuntimeException("Transport is shutting down");
        }
        
        if (processInput == null || !connected) {
            throw new RuntimeException("Process not connected");
        }
        
        try {
            processInput.write(message);
            processInput.newLine();
            processInput.flush();
            logger.debug("Sent message to process: {}", message);
        } catch (Exception e) {
            logger.error("Failed to send message to process: {}", String.join(" ", command), e);
            throw new RuntimeException("Failed to send message to process", e);
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected && !shuttingDown && process != null && process.isAlive();
    }
    
    private void readOutput() {
        try {
            String line;
            while (!shuttingDown && (line = processOutput.readLine()) != null) {
                handleIncomingMessage(line);
            }
        } catch (IOException e) {
            if (!shuttingDown) {
                logger.error("Error reading process output", e);
            }
        }
    }
}
