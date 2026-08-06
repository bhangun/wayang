package tech.kayys.wayang.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Chain - Fluent chain for operations
 */
public class Chain<T> {
    private final T value;
    private final List<Function<T, T>> operations = new ArrayList<>();
    
    public Chain(T initial) {
        this.value = initial;
    }
    
    public Chain<T> then(Function<T, T> operation) {
        operations.add(operation);
        return this;
    }
    
    public T execute() {
        T result = value;
        for (Function<T, T> op : operations) {
            result = op.apply(result);
        }
        return result;
    }
}
