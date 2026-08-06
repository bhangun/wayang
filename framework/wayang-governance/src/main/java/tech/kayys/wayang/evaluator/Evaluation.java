package tech.kayys.wayang.evaluator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Evaluation - complete evaluation model
 */
public record Evaluation(
    String id,
    String name,
    double score,
    Map<String, Double> metrics,
    String feedback,
    EvaluationStatus status,
    List<EvaluationIssue> issues,
    Map<String, Object> metadata,
    long evaluationTimeMs,
    Instant timestamp
) {
    public static Evaluation of(double score, EvaluationStatus status) {
        return new Evaluation(
            Id.random().asString(),
            null,
            score,
            Map.of(),
            null,
            status,
            List.of(),
            Map.of(),
            0,
            Instant.now()
        );
    }
    
    public static Evaluation passed(double score) {
        return new Evaluation(
            Id.random().asString(),
            null,
            score,
            Map.of(),
            null,
            EvaluationStatus.PASSED,
            List.of(),
            Map.of(),
            0,
            Instant.now()
        );
    }
    
    public static Evaluation failed(double score, String feedback) {
        return new Evaluation(
            Id.random().asString(),
            null,
            score,
            Map.of(),
            feedback,
            EvaluationStatus.FAILED,
            List.of(),
            Map.of(),
            0,
            Instant.now()
        );
    }
    
    public Evaluation withMetric(String key, double value) {
        Map<String, Double> newMetrics = new HashMap<>(metrics);
        newMetrics.put(key, value);
        return new Evaluation(id, name, score, newMetrics, feedback, status, 
            issues, metadata, evaluationTimeMs, timestamp);
    }
    
    public Evaluation withIssue(EvaluationIssue issue) {
        List<EvaluationIssue> newIssues = new ArrayList<>(issues);
        newIssues.add(issue);
        return new Evaluation(id, name, score, metrics, feedback, status, 
            newIssues, metadata, evaluationTimeMs, timestamp);
    }
}
