package tech.kayys.wayang.memory;

/**
 * Defines the lifetime and boundaries of a memory item.
 */
public enum MemoryScope {
    /** Only useful while a single run is executing. */
    EXECUTION,
    
    /** Useful inside one conversation. Doesn't automatically become project memory. */
    CONVERSATION,
    
    /** Available across conversations inside a project. The most important scope. */
    PROJECT,
    
    /** User-level preferences spanning projects (opt-in). */
    USER,
    
    /** Organization-level knowledge. Very restricted. */
    TENANT
}
