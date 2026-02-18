package tech.kayys.wayang.control.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ApplicationScoped
public class ControlPlaneGrpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(ControlPlaneGrpcServer.class);

    @Inject
    ProjectGrpcServiceImpl projectService;

    @Inject
    WorkflowGrpcServiceImpl workflowService;

    private Server server;

    public void onStart(@Observes StartupEvent event) {
        try {
            start();
        } catch (IOException e) {
            LOG.error("Failed to start gRPC server", e);
        }
    }

    public void start() throws IOException {
        int port = 9091; // Default port for control plane gRPC
        
        server = ServerBuilder.forPort(port)
                .addService(projectService)
                .addService(workflowService)
                // Add other services as needed
                .build()
                .start();
        
        LOG.info("Control Plane gRPC Server started, listening on port " + port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down gRPC server...");
            ControlPlaneGrpcServer.this.stop();
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