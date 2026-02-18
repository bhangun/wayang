package tech.kayys.wayang.eip.strategy;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SplitStrategyFactory {

    private final Map<String, SplitStrategy> strategies = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        strategies.put("fixed", new FixedSizeSplitStrategy());
        strategies.put("delimiter", new DelimiterSplitStrategy());
        strategies.put("json-array", new JsonArraySplitStrategy());
    }

    public SplitStrategy getStrategy(String name) {
        SplitStrategy strategy = strategies.get(name);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown split strategy: " + name);
        }
        return strategy;
    }
}
