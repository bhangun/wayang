package tech.kayys.wayang.harness.spi;
public interface HarnessPhase<I, O> {
    O execute(I input, HarnessContext context, HarnessRuntime runtime);
}
