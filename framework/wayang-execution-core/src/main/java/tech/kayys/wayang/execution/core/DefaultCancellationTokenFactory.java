package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.execution.CancellationToken;
import tech.kayys.wayang.execution.CancellationTokenFactory;

/**
 * Default factory that creates {@link DefaultCancellationToken} instances.
 */
public final class DefaultCancellationTokenFactory implements CancellationTokenFactory {

    @Override
    public CancellationToken create() {
        return new DefaultCancellationToken();
    }

    @Override
    public CancellationToken createChild(CancellationToken parent) {
        DefaultCancellationToken child = new DefaultCancellationToken();
        parent.onCancel(child::cancel);
        return child;
    }
}
