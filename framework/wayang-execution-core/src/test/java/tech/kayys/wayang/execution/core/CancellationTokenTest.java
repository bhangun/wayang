package tech.kayys.wayang.execution.core;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.execution.CancellationToken;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CancellationTokenTest {

    @Test
    void freshTokenIsNotCancelled() {
        CancellationToken token = new DefaultCancellationToken();
        assertFalse(token.isCancelled());
    }

    @Test
    void cancelSetsCancelledState() {
        CancellationToken token = new DefaultCancellationToken();
        token.cancel();
        assertTrue(token.isCancelled());
    }

    @Test
    void callbackFiredOnCancel() {
        AtomicBoolean called = new AtomicBoolean(false);
        CancellationToken token = new DefaultCancellationToken();
        token.onCancel(() -> called.set(true));
        token.cancel();
        assertTrue(called.get());
    }

    @Test
    void callbackFiredImmediatelyIfAlreadyCancelled() {
        CancellationToken token = new DefaultCancellationToken();
        token.cancel();
        AtomicBoolean called = new AtomicBoolean(false);
        token.onCancel(() -> called.set(true));
        assertTrue(called.get());
    }

    @Test
    void childTokenCancelledWhenParentCancelled() {
        DefaultCancellationTokenFactory factory = new DefaultCancellationTokenFactory();
        CancellationToken parent = factory.create();
        CancellationToken child = factory.createChild(parent);

        assertFalse(child.isCancelled());
        parent.cancel();
        assertTrue(child.isCancelled());
    }
}
