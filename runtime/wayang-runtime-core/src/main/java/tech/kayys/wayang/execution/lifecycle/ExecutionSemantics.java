package tech.kayys.wayang.execution.lifecycle;

public enum ExecutionSemantics {

    /**
     * An execution may be delivered/executed more than once.
     * Executors must therefore be idempotent where side effects exist.
     */
    AT_LEAST_ONCE,

    /**
     * The runtime guarantees one logical execution attempt
     * within its durable execution model.
     *
     * External side effects still require idempotency support.
     */
    EXACTLY_ONCE
}
