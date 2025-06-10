package tech.kayys.wayang.mcp.client.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class WayangMcpClientProcessor {

    private static final String FEATURE = "wayang-mcp-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
