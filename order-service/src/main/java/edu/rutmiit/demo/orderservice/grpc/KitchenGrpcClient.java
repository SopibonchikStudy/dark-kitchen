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

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class KitchenGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(KitchenGrpcClient.class);

    @Value("${grpc.client.kitchen.host:localhost}")
    private String kitchenHost;

    @Value("${grpc.client.kitchen.port:9092}")
    private int kitchenPort;

    private ManagedChannel channel;
    private KitchenServiceGrpc.KitchenServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder
                .forAddress(kitchenHost, kitchenPort)
                .usePlaintext()
                .build();

        stub = KitchenServiceGrpc.newBlockingStub(channel);
        log.info("gRPC клиент кухни подключён к {}:{}", kitchenHost, kitchenPort);
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
     * Получить статус заказа на кухне
     */
    public OrderStatusResponse getOrderStatus(String orderId) {
        log.debug("gRPC запрос статуса заказа: {}", orderId);

        GetOrderStatusRequest request = GetOrderStatusRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        return stub.getOrderStatus(request);
    }

    /**
     * Оценить время приготовления блюд
     */
    public EstimateCookingTimeResponse estimateCookingTime(List<String> menuItemIds) {
        log.debug("gRPC запрос времени приготовления для {} блюд", menuItemIds.size());

        EstimateCookingTimeRequest request = EstimateCookingTimeRequest.newBuilder()
                .addAllMenuItemIds(menuItemIds)
                .build();

        return stub.estimateCookingTime(request);
    }
}