package org.example.userservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    @Value("${grpc.server.port:9091}")
    private int grpcPort;

    private Server server;

    @Bean
    public ApplicationRunner startGrpcServer(UserGrpcServer userGrpcServer) {
        return args -> {
            server = ServerBuilder
                    .forPort(grpcPort)
                    .addService(userGrpcServer)
                    .build()
                    .start();
            logger.info("gRPC server started on port {}", grpcPort);
        };
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null && !server.isShutdown()) {
            server.shutdown();
        }
    }
}