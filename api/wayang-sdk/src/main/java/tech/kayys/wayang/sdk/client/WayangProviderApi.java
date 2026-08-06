package tech.kayys.wayang.sdk.client;

import tech.kayys.wayang.sdk.provider.Provider;

/**
 * API for managing inference providers.
 */
public final class WayangProviderApi {

    private Provider defaultProvider;

    public WayangProviderApi() {
    }

    public void setDefaultProvider(Provider provider) {
        this.defaultProvider = provider;
    }

    public Provider getDefaultProvider() {
        return defaultProvider;
    }
}
