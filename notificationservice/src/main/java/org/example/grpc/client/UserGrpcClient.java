package org.example.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.example.grpc.UserGrpcServiceGrpc;
import org.example.grpc.UserRequest;
import org.example.grpc.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserGrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(UserGrpcClient.class);

    private final ManagedChannel channel;
    private final UserGrpcServiceGrpc.UserGrpcServiceBlockingStub stub;

    public UserGrpcClient(
            @Value("${grpc.userservice.host:userservice}") String host,
            @Value("${grpc.userservice.port:9091}") int port) {

        this.channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build();

        this.stub = UserGrpcServiceGrpc.newBlockingStub(channel);
        logger.info("gRPC client connected to userservice at {}:{}", host, port);
    }

    public UserResponse getUserById(Long userId) {
        try {
            return stub.getUserById(
                UserRequest.newBuilder().setUserId(userId).build()
            );
        } catch (Exception e) {
            logger.error("gRPC call to userservice failed for userId {}: {}", userId, e.getMessage());
            return UserResponse.newBuilder().setFound(false).build();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (!channel.isShutdown()) channel.shutdown();
    }
}
