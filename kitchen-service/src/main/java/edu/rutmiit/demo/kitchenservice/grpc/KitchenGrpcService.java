package edu.rutmiit.demo.kitchenservice.grpc;

import edu.rutmiit.demo.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KitchenGrpcService extends KitchenServiceGrpc.KitchenServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(KitchenGrpcService.class);

    // Время приготовления для блюд (секунды)
    private static final Map<String, Integer> COOKING_TIMES = Map.of(
            "burger-001", 300,
            "burger-002", 240,
            "pizza-001", 480,
            "pizza-002", 420,
            "salad-001", 120,
            "drink-001", 30,
            "drink-002", 30
    );

    // Статусы заказов
    private final ConcurrentHashMap<String, OrderStatus> orderStatuses = new ConcurrentHashMap<>();

    @Override
    public void getOrderStatus(GetOrderStatusRequest request,
                               StreamObserver<OrderStatusResponse> responseObserver) {
        String orderId = request.getOrderId();
        log.info("gRPC запрос статуса заказа: {}", orderId);

        OrderStatus status = orderStatuses.getOrDefault(orderId,
                new OrderStatus("UNKNOWN", "Заказ не найден", 0));

        OrderStatusResponse response = OrderStatusResponse.newBuilder()
                .setOrderId(orderId)
                .setStatus(status.status)
                .setMessage(status.message)
                .setEstimatedSeconds(status.remainingSeconds)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void estimateCookingTime(EstimateCookingTimeRequest request,
                                    StreamObserver<EstimateCookingTimeResponse> responseObserver) {
        log.info("gRPC запрос времени приготовления для {} блюд", request.getMenuItemIdsCount());

        EstimateCookingTimeResponse.Builder responseBuilder =
                EstimateCookingTimeResponse.newBuilder();

        int totalSeconds = 0;
        for (String itemId : request.getMenuItemIdsList()) {
            int seconds = COOKING_TIMES.getOrDefault(itemId, 180);
            totalSeconds += seconds;

            responseBuilder.addItems(ItemCookingTime.newBuilder()
                    .setMenuItemId(itemId)
                    .setSeconds(seconds)
                    .build());
        }

        responseBuilder.setTotalSeconds(totalSeconds);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    public void updateOrderStatus(String orderId, String status, String message, int remainingSeconds) {
        orderStatuses.put(orderId, new OrderStatus(status, message, remainingSeconds));
    }

    record OrderStatus(String status, String message, int remainingSeconds) {}

}