package org.example.inventoryservice.grpc;

import io.grpc.stub.StreamObserver;
import org.example.grpc.InventoryGrpcServiceGrpc;
import org.example.grpc.SeatRequest;
import org.example.grpc.SeatResponse;
import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InventoryGrpcServer extends InventoryGrpcServiceGrpc.InventoryGrpcServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(InventoryGrpcServer.class);
    private final SeatRepository seatRepository;

    public InventoryGrpcServer(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public void checkSeatAvailability(SeatRequest request,
                                      StreamObserver<SeatResponse> responseObserver) {
        logger.info("gRPC: CheckSeatAvailability seatId={} eventId={} userId={}",
                request.getSeatId(), request.getTicketEventId(), request.getUserId());

        Seat seat = seatRepository.findById(request.getSeatId()).orElse(null);

        SeatResponse response;

        if (seat == null) {
            response = SeatResponse.newBuilder()
                    .setAvailable(false).setReason("SEAT_NOT_FOUND").build();

        } else if (!seat.getTicketEventId().equals(request.getTicketEventId())) {
            response = SeatResponse.newBuilder()
                    .setAvailable(false).setReason("SEAT_DOES_NOT_BELONG_TO_EVENT").build();

        } else if (seat.getStatus() == Seat.SeatStatus.SOLD) {
            response = SeatResponse.newBuilder()
                    .setAvailable(false)
                    .setSeatNumber(seat.getSeatNumber())
                    .setSection(seat.getSection())
                    .setReason("SEAT_SOLD").build();

        } else if (seat.getStatus() == Seat.SeatStatus.HELD) {
            // Allow if this user holds the seat (click-to-hold)
            boolean heldByThisUser = request.getUserId() > 0 &&
                    Long.valueOf(request.getUserId()).equals(seat.getHeldByUserId());

            if (heldByThisUser) {
                response = SeatResponse.newBuilder()
                        .setAvailable(true)
                        .setSeatNumber(seat.getSeatNumber())
                        .setSection(seat.getSection())
                        .setPrice(seat.getPrice().doubleValue())
                        .build();
            } else {
                response = SeatResponse.newBuilder()
                        .setAvailable(false)
                        .setSeatNumber(seat.getSeatNumber())
                        .setSection(seat.getSection())
                        .setReason("SEAT_HELD").build();
            }

        } else {
            response = SeatResponse.newBuilder()
                    .setAvailable(true)
                    .setSeatNumber(seat.getSeatNumber())
                    .setSection(seat.getSection())
                    .setPrice(seat.getPrice().doubleValue())
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
