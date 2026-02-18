package tech.kayys.wayang.eip.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

public record IntegrationPluginContext(
        CamelContext camelContext,
        ProducerTemplate producerTemplate,
        ObjectMapper objectMapper) {
}
