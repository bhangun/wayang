package tech.kayys.wayang.health;


/**
 * Default Metrics Implementation
 */
public class DefaultMetrics implements Metrics {
    
    private final String namespace;
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Double>> gauges = new ConcurrentHashMap<>();
    private final Map<String, HistogramData> histograms = new ConcurrentHashMap<>();
    private final Map<String, TimerData> timers = new ConcurrentHashMap<>();
    
    public DefaultMetrics(String namespace) {
        this.namespace = namespace;
    }
    
    @Override
    public void counter(String name, long increment) {
        String key = namespace + "." + name;
        counters.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(increment);
    }
    
    @Override
    public void counter(String name, long increment, Map<String, String> tags) {
        String key = namespace + "." + name + ":" + tags;
        counters.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(increment);
    }
    
    @Override
    public void gauge(String name, double value) {
        String key = namespace + "." + name;
        gauges.computeIfAbsent(key, k -> new AtomicReference<>()).set(value);
    }
    
    @Override
    public void gauge(String name, double value, Map<String, String> tags) {
        String key = namespace + "." + name + ":" + tags;
        gauges.computeIfAbsent(key, k -> new AtomicReference<>()).set(value);
    }
    
    @Override
    public void histogram(String name, double value) {
        String key = namespace + "." + name;
        histograms.computeIfAbsent(key, k -> new HistogramData()).record(value);
    }
    
    @Override
    public void histogram(String name, double value, Map<String, String> tags) {
        String key = namespace + "." + name + ":" + tags;
        histograms.computeIfAbsent(key, k -> new HistogramData()).record(value);
    }
    
    @Override
    public void timer(String name, long durationMs) {
        String key = namespace + "." + name;
        timers.computeIfAbsent(key, k -> new TimerData()).record(durationMs);
    }
    
    @Override
    public void timer(String name, long durationMs, Map<String, String> tags) {
        String key = namespace + "." + name + ":" + tags;
        timers.computeIfAbsent(key, k -> new TimerData()).record(durationMs);
    }
    
    @Override
    public Map<String, MetricValue> snapshot() {
        Map<String, MetricValue> result = new LinkedHashMap<>();
        
        // Counters
        for (Map.Entry<String, AtomicLong> entry : counters.entrySet()) {
            result.put(entry.getKey(), new MetricValue(
                entry.getKey(),
                MetricType.COUNTER,
                entry.getValue().doubleValue(),
                1, 0, 0, 0, 0, 0, 0,
                Map.of()
            ));
        }
        
        // Gauges
        for (Map.Entry<String, AtomicReference<Double>> entry : gauges.entrySet()) {
            Double value = entry.getValue().get();
            if (value != null) {
                result.put(entry.getKey(), new MetricValue(
                    entry.getKey(),
                    MetricType.GAUGE,
                    value,
                    1, 0, 0, 0, 0, 0, 0,
                    Map.of()
                ));
            }
        }
        
        // Histograms
        for (Map.Entry<String, HistogramData> entry : histograms.entrySet()) {
            HistogramData data = entry.getValue();
            result.put(entry.getKey(), new MetricValue(
                entry.getKey(),
                MetricType.HISTOGRAM,
                data.getMean(),
                data.getCount(),
                data.getMin(),
                data.getMax(),
                data.getMean(),
                data.getPercentile(50),
                data.getPercentile(95),
                data.getPercentile(99),
                Map.of()
            ));
        }
        
        // Timers
        for (Map.Entry<String, TimerData> entry : timers.entrySet()) {
            TimerData data = entry.getValue();
            result.put(entry.getKey(), new MetricValue(
                entry.getKey(),
                MetricType.TIMER,
                data.getMean(),
                data.getCount(),
                data.getMin(),
                data.getMax(),
                data.getMean(),
                data.getPercentile(50),
                data.getPercentile(95),
                data.getPercentile(99),
                Map.of()
            ));
        }
        
        return result;
    }
    
    @Override
    public void reset() {
        counters.clear();
        gauges.clear();
        histograms.clear();
        timers.clear();
    }
    
    /**
     * Histogram Data with percentiles
     */
    private static class HistogramData {
        private final List<Double> values = Collections.synchronizedList(new ArrayList<>());
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicDouble sum = new AtomicDouble(0);
        private final AtomicDouble min = new AtomicDouble(Double.MAX_VALUE);
        private final AtomicDouble max = new AtomicDouble(Double.MIN_VALUE);
        
        synchronized void record(double value) {
            values.add(value);
            count.incrementAndGet();
            sum.addAndGet(value);
            min.updateAndGet(m -> Math.min(m, value));
            max.updateAndGet(m -> Math.max(m, value));
            
            // Keep at most 10000 values
            if (values.size() > 10000) {
                values.remove(0);
            }
        }
        
        long getCount() { return count.get(); }
        double getMean() { return count.get() > 0 ? sum.get() / count.get() : 0; }
        double getMin() { return min.get() == Double.MAX_VALUE ? 0 : min.get(); }
        double getMax() { return max.get() == Double.MIN_VALUE ? 0 : max.get(); }
        
        synchronized double getPercentile(double p) {
            if (values.isEmpty()) return 0;
            double[] sorted = values.stream().sorted().mapToDouble(Double::doubleValue).toArray();
            int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
            return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
        }
    }
    
    /**
     * Timer Data
     */
    private static class TimerData {
        private final List<Long> values = Collections.synchronizedList(new ArrayList<>());
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong sum = new AtomicLong(0);
        private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);
        
        synchronized void record(long value) {
            values.add(value);
            count.incrementAndGet();
            sum.addAndGet(value);
            min.updateAndGet(m -> Math.min(m, value));
            max.updateAndGet(m -> Math.max(m, value));
            
            if (values.size() > 10000) {
                values.remove(0);
            }
        }
        
        long getCount() { return count.get(); }
        double getMean() { return count.get() > 0 ? (double) sum.get() / count.get() : 0; }
        long getMin() { return min.get() == Long.MAX_VALUE ? 0 : min.get(); }
        long getMax() { return max.get() == Long.MIN_VALUE ? 0 : max.get(); }
        
        synchronized double getPercentile(double p) {
            if (values.isEmpty()) return 0;
            long[] sorted = values.stream().sorted().mapToLong(Long::longValue).toArray();
            int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
            return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
        }
    }
}