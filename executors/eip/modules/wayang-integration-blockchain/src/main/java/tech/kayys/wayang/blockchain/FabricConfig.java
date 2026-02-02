package tech.kayys.gamelan.executor.camel.blockchain;

record FabricConfig(
        String networkConfigPath,
        String walletPath,
        String organizationName,
        String userName) {
}