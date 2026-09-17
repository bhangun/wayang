package tech.kayys.wayang.spi.capability;

/**
 * Functional SPI for invoking a capability with a given invocation context.
 */
@FunctionalInterface
public interface CapabilityInvoker {

    /**
     * Executes the capability within the provided context.
     *
     * @param context the invocation context
     * @return the result of execution
     * @throws Exception if execution fails fatally
     */
    CapabilityInvocationResult invoke(CapabilityInvocationContext context) throws Exception;
}
