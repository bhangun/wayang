package tech.kayys.wayang.provider;

/**
 * Defines the invocation strategy for a {@link Provider}.
 * This is particularly useful for providers (like Gollek) that support
 * multiple integration models such as running embedded locally or
 * connecting to a remote cluster via HTTP/gRPC.
 */
public enum ProviderStrategy {
    /**
     * The provider runs embedded within the same JVM instance.
     */
    EMBEDDED,
    
    /**
     * The provider communicates with a remote service via HTTP REST APIs.
     */
    REST,
    
    /**
     * The provider communicates with a remote service via gRPC.
     */
    GRPC
}
