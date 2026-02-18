package tech.kayys.wayang.eip.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

@ApplicationScoped
public class IntegrationRuntimeBeans {

    @Produces
    @ApplicationScoped
    CamelContext camelContext() {
        return new DefaultCamelContext();
    }

    @Produces
    @ApplicationScoped
    ProducerTemplate producerTemplate(CamelContext camelContext) {
        return camelContext.createProducerTemplate();
    }
}
