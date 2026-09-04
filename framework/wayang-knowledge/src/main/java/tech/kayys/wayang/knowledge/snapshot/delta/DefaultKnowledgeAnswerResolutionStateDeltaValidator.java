package tech.kayys.wayang.knowledge.snapshot.delta;

import tech.kayys.wayang.knowledge.exchange.statemachine.DefaultKnowledgeAnswerResolutionStateFingerprinter;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionStateFingerprinter;

public final class DefaultKnowledgeAnswerResolutionStateDeltaValidator
        implements KnowledgeAnswerResolutionStateDeltaValidator {

    private final KnowledgeAnswerResolutionStateFingerprinter fingerprinter;

    public DefaultKnowledgeAnswerResolutionStateDeltaValidator(
            KnowledgeAnswerResolutionStateFingerprinter fingerprinter) {
        this.fingerprinter = fingerprinter != null
                ? fingerprinter
                : new DefaultKnowledgeAnswerResolutionStateFingerprinter();
    }

    public DefaultKnowledgeAnswerResolutionStateDeltaValidator() {
        this(new DefaultKnowledgeAnswerResolutionStateFingerprinter());
    }

    @Override
    public boolean validate(
            KnowledgeAnswerResolutionState source,
            KnowledgeAnswerResolutionStateDelta delta) {

        if (source == null || delta == null) {
            return false;
        }

        String sourceFp = fingerprinter.fingerprint(source);
        return sourceFp.equals(delta.sourceStateFingerprint());
    }
}
