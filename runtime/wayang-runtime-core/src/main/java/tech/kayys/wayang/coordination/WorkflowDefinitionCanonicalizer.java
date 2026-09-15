package tech.kayys.wayang.coordination;

/**
 * Service for producing deterministic canonical JSON representations of workflow definitions
 * and calculating their stable fingerprints.
 */
public interface WorkflowDefinitionCanonicalizer {

    String canonicalize(CoordinationWorkflowDefinition definition);

    default WorkflowDefinitionFingerprint fingerprint(CoordinationWorkflowDefinition definition) {
        return WorkflowDefinitionFingerprint.sha256(canonicalize(definition));
    }
}
