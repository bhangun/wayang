package tech.kayys.wayang.spi.memory;

import java.util.List;

/**
 * SPI for managing an agent's memory (e.g., conversation history, facts).
 * 
 * @param <T> The type of the message (e.g., ChatMessage).
 */
public interface Memory<T> {

    /**
     * Appends a message to the memory context.
     * 
     * @param message the message to add
     */
    void addMessage(T message);

    /**
     * Retrieves the entire memory history.
     * 
     * @return a list of all stored messages in chronological order
     */
    List<T> getHistory();

    /**
     * Clears the current memory context.
     */
    void clear();
}
