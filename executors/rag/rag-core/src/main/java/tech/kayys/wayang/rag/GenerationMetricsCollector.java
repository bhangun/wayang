package main.java.tech.kayys.wayang.rag;

@ApplicationScoped
public class GenerationMetricsCollector {

    private static final Logger LOG = LoggerFactory.getLogger(GenerationMetricsCollector.class);

    public void recordGeneration(String workflowRunId, int tokensUsed, long durationMs) {
        LOG.info("Generation metrics - Run: {}, Tokens: {}, Duration: {}ms",
                workflowRunId, tokensUsed, durationMs);
    }
}