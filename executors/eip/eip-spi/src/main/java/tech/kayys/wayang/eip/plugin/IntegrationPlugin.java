package tech.kayys.wayang.eip.plugin;

import java.util.Map;

/**
 * Contract for pluggable integration modules.
 */
public interface IntegrationPlugin {

    String id();

    String description();

    IntegrationDeployment deploy(IntegrationPluginContext context, Map<String, Object> options) throws Exception;

    default void undeploy(IntegrationPluginContext context, IntegrationDeployment deployment) throws Exception {
        for (String routeId : deployment.routeIds()) {
            if (context.camelContext().getRoute(routeId) != null) {
                context.camelContext().getRouteController().stopRoute(routeId);
                context.camelContext().removeRoute(routeId);
            }
        }
    }
}
