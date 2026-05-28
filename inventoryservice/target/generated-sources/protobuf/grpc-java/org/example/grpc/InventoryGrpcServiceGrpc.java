package org.example.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class InventoryGrpcServiceGrpc {

  private InventoryGrpcServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "InventoryGrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.grpc.SeatRequest,
      org.example.grpc.SeatResponse> getCheckSeatAvailabilityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CheckSeatAvailability",
      requestType = org.example.grpc.SeatRequest.class,
      responseType = org.example.grpc.SeatResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.SeatRequest,
      org.example.grpc.SeatResponse> getCheckSeatAvailabilityMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.SeatRequest, org.example.grpc.SeatResponse> getCheckSeatAvailabilityMethod;
    if ((getCheckSeatAvailabilityMethod = InventoryGrpcServiceGrpc.getCheckSeatAvailabilityMethod) == null) {
      synchronized (InventoryGrpcServiceGrpc.class) {
        if ((getCheckSeatAvailabilityMethod = InventoryGrpcServiceGrpc.getCheckSeatAvailabilityMethod) == null) {
          InventoryGrpcServiceGrpc.getCheckSeatAvailabilityMethod = getCheckSeatAvailabilityMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.SeatRequest, org.example.grpc.SeatResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CheckSeatAvailability"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.SeatRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.SeatResponse.getDefaultInstance()))
              .setSchemaDescriptor(new InventoryGrpcServiceMethodDescriptorSupplier("CheckSeatAvailability"))
              .build();
        }
      }
    }
    return getCheckSeatAvailabilityMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static InventoryGrpcServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceStub>() {
        @java.lang.Override
        public InventoryGrpcServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryGrpcServiceStub(channel, callOptions);
        }
      };
    return InventoryGrpcServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static InventoryGrpcServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceBlockingV2Stub>() {
        @java.lang.Override
        public InventoryGrpcServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryGrpcServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return InventoryGrpcServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static InventoryGrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceBlockingStub>() {
        @java.lang.Override
        public InventoryGrpcServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryGrpcServiceBlockingStub(channel, callOptions);
        }
      };
    return InventoryGrpcServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static InventoryGrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InventoryGrpcServiceFutureStub>() {
        @java.lang.Override
        public InventoryGrpcServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InventoryGrpcServiceFutureStub(channel, callOptions);
        }
      };
    return InventoryGrpcServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Called by orderservice before placing an order
     * Returns whether the seat is available and its details
     * </pre>
     */
    default void checkSeatAvailability(org.example.grpc.SeatRequest request,
        io.grpc.stub.StreamObserver<org.example.grpc.SeatResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckSeatAvailabilityMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service InventoryGrpcService.
   */
  public static abstract class InventoryGrpcServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return InventoryGrpcServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service InventoryGrpcService.
   */
  public static final class InventoryGrpcServiceStub
      extends io.grpc.stub.AbstractAsyncStub<InventoryGrpcServiceStub> {
    private InventoryGrpcServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryGrpcServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryGrpcServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Called by orderservice before placing an order
     * Returns whether the seat is available and its details
     * </pre>
     */
    public void checkSeatAvailability(org.example.grpc.SeatRequest request,
        io.grpc.stub.StreamObserver<org.example.grpc.SeatResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckSeatAvailabilityMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service InventoryGrpcService.
   */
  public static final class InventoryGrpcServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<InventoryGrpcServiceBlockingV2Stub> {
    private InventoryGrpcServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryGrpcServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryGrpcServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Called by orderservice before placing an order
     * Returns whether the seat is available and its details
     * </pre>
     */
    public org.example.grpc.SeatResponse checkSeatAvailability(org.example.grpc.SeatRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCheckSeatAvailabilityMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service InventoryGrpcService.
   */
  public static final class InventoryGrpcServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<InventoryGrpcServiceBlockingStub> {
    private InventoryGrpcServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryGrpcServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryGrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Called by orderservice before placing an order
     * Returns whether the seat is available and its details
     * </pre>
     */
    public org.example.grpc.SeatResponse checkSeatAvailability(org.example.grpc.SeatRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckSeatAvailabilityMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service InventoryGrpcService.
   */
  public static final class InventoryGrpcServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<InventoryGrpcServiceFutureStub> {
    private InventoryGrpcServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InventoryGrpcServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InventoryGrpcServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Called by orderservice before placing an order
     * Returns whether the seat is available and its details
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.SeatResponse> checkSeatAvailability(
        org.example.grpc.SeatRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckSeatAvailabilityMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CHECK_SEAT_AVAILABILITY = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CHECK_SEAT_AVAILABILITY:
          serviceImpl.checkSeatAvailability((org.example.grpc.SeatRequest) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.SeatResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCheckSeatAvailabilityMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.grpc.SeatRequest,
              org.example.grpc.SeatResponse>(
                service, METHODID_CHECK_SEAT_AVAILABILITY)))
        .build();
  }

  private static abstract class InventoryGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    InventoryGrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.grpc.InventoryProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("InventoryGrpcService");
    }
  }

  private static final class InventoryGrpcServiceFileDescriptorSupplier
      extends InventoryGrpcServiceBaseDescriptorSupplier {
    InventoryGrpcServiceFileDescriptorSupplier() {}
  }

  private static final class InventoryGrpcServiceMethodDescriptorSupplier
      extends InventoryGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    InventoryGrpcServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (InventoryGrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new InventoryGrpcServiceFileDescriptorSupplier())
              .addMethod(getCheckSeatAvailabilityMethod())
              .build();
        }
      }
    }
    return result;
  }
}
