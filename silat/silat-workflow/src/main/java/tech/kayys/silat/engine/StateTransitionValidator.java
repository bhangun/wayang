package tech.kayys.silat.engine;

import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.silat.model.RunStatus;
import tech.kayys.silat.model.ValidationResult;

@ApplicationScoped
class StateTransitionValidator {

    private static final Map<RunStatus, Set<RunStatus>> ALLOWED = Map.of(
            RunStatus.CREATED, Set.of(RunStatus.RUNNING, RunStatus.CANCELED),
            RunStatus.RUNNING, Set.of(RunStatus.SUSPENDED, RunStatus.COMPLETED, RunStatus.FAILED, RunStatus.CANCELED),
            RunStatus.SUSPENDED, Set.of(RunStatus.RUNNING, RunStatus.CANCELED),
            RunStatus.COMPLETED, Set.of(),
            RunStatus.FAILED, Set.of(),
            RunStatus.CANCELED, Set.of());

    ValidationResult validate(RunStatus from, RunStatus to) {
        boolean allowed = ALLOWED.getOrDefault(from, Set.of()).contains(to);
        if (allowed) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Invalid transition " + from + " -> " + to);
        }
    }
}
