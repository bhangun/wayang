package tech.kayys.gamelan.executor.camel.modern;

record GraphQLConfig(
        String endpoint,
        String websocketEndpoint,
        String authToken) {
}