package edu.rutmiit.demo.orderservice.grpc;

import edu.rutmiit.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DeliveryGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(DeliveryGrpcClient.class);

    @Value("${grpc.client.delivery.host:localhost}")
    private String deliveryHost;

    @Value("${grpc.client.delivery.port:9093}")
    private int deliveryPort;

    private ManagedChannel channel;
    private DeliveryServiceGrpc.DeliveryServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder
                .forAddress(deliveryHost, deliveryPort)
                .usePlaintext()
                .build();

        stub = DeliveryServiceGrpc.newBlockingStub(channel);
        log.info("gRPC клиент доставки подключён к {}:{}", deliveryHost, deliveryPort);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Ошибка при остановке gRPC канала", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Получить статус доставки заказа
     */
    public DeliveryStatusResponse getDeliveryStatus(String orderId) {
        log.debug("gRPC запрос статуса доставки: {}", orderId);

        GetDeliveryStatusRequest request = GetDeliveryStatusRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        return stub.getDeliveryStatus(request);
    }

    /**
     * Найти ближайшего курьера
     */
    public CourierResponse findNearestCourier(String address) {
        log.debug("gRPC запрос поиска курьера для: {}", address);

        FindCourierRequest request = FindCourierRequest.newBuilder()
                .setDeliveryAddress(address)
                .build();

        return stub.findNearestCourier(request);
    }
}