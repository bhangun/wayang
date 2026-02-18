package tech.kayys.wayang.runtime.standalone.context;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tech.kayys.gollek.spi.context.RequestContext;

@ApplicationScoped
public class StandaloneRequestContexts {

    static final RequestContext DEFAULT_CONTEXT = RequestContext.of("community");

    @Produces
    RequestContext requestContext() {
        return DEFAULT_CONTEXT;
    }
}
