package tech.kayys.wayang.agent.service;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.kayys.wayang.agent.qualifier.AgentWebClient;

@ApplicationScoped
public class WebClientProducer {

    @Inject
    Vertx vertx;

    @Produces
    @Singleton
    @AgentWebClient
    public WebClient agentWebClient() {
        WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(10000)
                .setIdleTimeout(60)
                .setKeepAlive(true);
        return WebClient.create(vertx, options);
    }

    @Produces
    @Singleton
    @AgentWebClient
    public io.vertx.ext.web.client.WebClient agentCoreWebClient() {
        WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(10000)
                .setIdleTimeout(60)
                .setKeepAlive(true);
        return io.vertx.ext.web.client.WebClient.create(vertx.getDelegate(), options);
    }
}
