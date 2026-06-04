package org.example.orderservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.example.grpc.InventoryGrpcServiceGrpc;
import org.example.grpc.SeatRequest;
import org.example.grpc.SeatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryGrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryGrpcClient.class);

    private final ManagedChannel channel;
    private final InventoryGrpcServiceGrpc.InventoryGrpcServiceBlockingStub stub;

    public InventoryGrpcClient(
            @Value("${grpc.inventory.host:inventoryservice}") String host,
            @Value("${grpc.inventory.port:9090}") int port) {

        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub    = InventoryGrpcServiceGrpc.newBlockingStub(channel);
        logger.info("gRPC client connected to inventoryservice at {}:{}", host, port);
    }

    public SeatResponse checkSeatAvailability(Long seatId, Long ticketEventId,
                                              int quantity, Long userId) {
        SeatRequest request = SeatRequest.newBuilder()
                .setSeatId(seatId)
                .setTicketEventId(ticketEventId)
                .setQuantity(quantity)
                .setUserId(userId != null ? userId : 0L)
                .build();

        logger.info("gRPC: checking seat {} for event {} by user {}", seatId, ticketEventId, userId);
        return stub.checkSeatAvailability(request);
    }

    @PreDestroy
    public void shutdown() {
        if (!channel.isShutdown()) channel.shutdown();
    }
}
