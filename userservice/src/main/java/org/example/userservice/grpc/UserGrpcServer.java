package org.example.userservice.grpc;

import io.grpc.stub.StreamObserver;
import org.example.grpc.UserGrpcServiceGrpc;
import org.example.grpc.UserRequest;
import org.example.grpc.UserResponse;
import org.example.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserGrpcServer extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserGrpcServer.class);
    private final UserRepository userRepository;

    public UserGrpcServer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUserById(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        logger.info("gRPC: GetUserById id={}", request.getUserId());

        UserResponse response = userRepository.findById(request.getUserId())
                .map(user -> UserResponse.newBuilder()
                        .setId(user.getId())
                        .setUsername(user.getUsername())
                        .setEmail(user.getEmail())
                        .setName(user.getName())
                        .setFound(true)
                        .build())
                .orElse(UserResponse.newBuilder()
                        .setFound(false)
                        .build());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}