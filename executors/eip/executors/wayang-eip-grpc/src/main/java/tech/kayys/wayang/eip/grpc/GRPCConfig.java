package tech.kayys.wayang.eip.grpc;

record GRPCConfig(
                String host,
                int port,
                boolean useTLS,
                String certPath) {
}