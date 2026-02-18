package tech.kayys.wayang.schema.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;

@ApplicationScoped
public class GrpcServer {

    private static final Logger LOG = Logger.getLogger(GrpcServer.class);

    @Inject
    SchemaGrpcServiceImpl schemaService;

    private Server server;

    public void onStart(@Observes StartupEvent event) {
        try {
            start();
        } catch (IOException e) {
            LOG.error("Failed to start gRPC server", e);
        }
    }

    public void start() throws IOException {
        int port = 9090; // Default port, could be configurable
        
        server = ServerBuilder.forPort(port)
                .addService(schemaService)
                .build()
                .start();
        
        LOG.info("gRPC Server started, listening on port " + port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down gRPC server...");
            GrpcServer.this.stop();
            LOG.info("gRPC server shut down");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}