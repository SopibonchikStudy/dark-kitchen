package edu.rutmiit.demo.deliveryservice.grpc;

import edu.rutmiit.demo.deliveryservice.model.Courier;
import edu.rutmiit.demo.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeliveryGrpcService extends DeliveryServiceGrpc.DeliveryServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(DeliveryGrpcService.class);

    // Статусы доставки
    private final ConcurrentHashMap<String, DeliveryInfo> deliveryStatuses = new ConcurrentHashMap<>();

    @Override
    public void getDeliveryStatus(GetDeliveryStatusRequest request,
                                  StreamObserver<DeliveryStatusResponse> responseObserver) {
        String orderId = request.getOrderId();
        log.info("gRPC запрос статуса доставки: {}", orderId);

        DeliveryInfo info = deliveryStatuses.getOrDefault(orderId,
                new DeliveryInfo("UNKNOWN", null, "Заказ не найден", 0));

        DeliveryStatusResponse.Builder responseBuilder = DeliveryStatusResponse.newBuilder()
                .setOrderId(orderId)
                .setStatus(info.status)
                .setEstimatedMinutes(info.estimatedMinutes);

        if (info.courier != null) {
            responseBuilder
                    .setCourierId(info.courier.id())
                    .setCourierName(info.courier.name());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findNearestCourier(FindCourierRequest request,
                                   StreamObserver<CourierResponse> responseObserver) {
        String address = request.getDeliveryAddress();
        log.info("gRPC запрос поиска курьера для адреса: {}", address);

        // Заглушка - возвращаем фиксированного курьера
        CourierResponse response = CourierResponse.newBuilder()
                .setCourierId("COUR-001")
                .setCourierName("Алексей Смирнов")
                .setCourierPhone("+79001112233")
                .setEstimatedMinutes(15)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateDeliveryStatus(String orderId, String status, Courier courier, int minutes) {
        deliveryStatuses.put(orderId, new DeliveryInfo(status, courier, "", minutes));
    }

    record DeliveryInfo(String status, Courier courier, String message, int estimatedMinutes) {}
}