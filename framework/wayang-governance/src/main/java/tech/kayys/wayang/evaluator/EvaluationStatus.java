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


/**
 * Evaluation Status
 */
public enum EvaluationStatus {
    PASSED,
    FAILED,
    PARTIAL,
    PENDING,
    ERROR,
    SKIPPED
}