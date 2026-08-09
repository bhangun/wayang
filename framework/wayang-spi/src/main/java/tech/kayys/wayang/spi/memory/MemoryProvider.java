package tech.kayys.wayang.spi.memory;

/**
 * SPI for creating/resolving {@link Memory} instances.
 */
public interface MemoryProvider {

    /**
     * Creates or retrieves a memory context for a given session.
     *
     * @param sessionId the unique identifier for the conversation/session
     * @param <T> the type of messages stored in the memory
     * @return the memory instance
     */
    <T> Memory<T> createMemory(String sessionId);
}
