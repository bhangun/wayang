package com.contextcompiler.quarkus;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "context-compiler")
public interface ContextCompilerConfig {

    @WithDefault("2")
    int defaultMaxHops();

    @WithDefault("false")
    boolean typeAwareResolution();

    /** Used by /compile/planned when the caller doesn't specify a window size -- default assumes a small open-weight model. */
    @WithDefault("8000")
    long defaultModelContextWindowTokens();
}
