package tech.kayys.wayang.runtime.standalone.context;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.SecurityContext;
import tech.kayys.gollek.spi.context.RequestContext;
import tech.kayys.gollek.spi.context.RequestContextResolver;

@ApplicationScoped
public class StandaloneRequestContextResolver implements RequestContextResolver {

    @Override
    public RequestContext resolve(SecurityContext securityContext) {
        return StandaloneRequestContexts.DEFAULT_CONTEXT;
    }
}
