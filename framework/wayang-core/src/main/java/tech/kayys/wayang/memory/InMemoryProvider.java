package tech.kayys.wayang.memory;

import tech.kayys.wayang.spi.memory.Memory;
import tech.kayys.wayang.spi.memory.MemoryProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple, volatile, in-memory implementation of {@link MemoryProvider}.
 * Ideal for short-lived sessions or stateless environments.
 */
public class InMemoryProvider implements MemoryProvider {

    private final Map<String, Memory<?>> sessions = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Memory<T> createMemory(String sessionId) {
        return (Memory<T>) sessions.computeIfAbsent(sessionId, id -> new InMemory<T>());
    }

    private static class InMemory<T> implements Memory<T> {
        private final List<T> history = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void addMessage(T message) {
            history.add(message);
        }

        @Override
        public List<T> getHistory() {
            return new ArrayList<>(history);
        }

        @Override
        public void clear() {
            history.clear();
        }
    }
}
