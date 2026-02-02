package tech.kayys.gamelan.executor.camel.blockchain;

record EthereumConfig(
        String nodeUrl,
        String network,
        String privateKey) {
}