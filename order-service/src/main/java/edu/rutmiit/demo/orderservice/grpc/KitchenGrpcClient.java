package edu.rutmiit.demo.orderservice.grpc;

import edu.rutmiit.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

    // ✅ Ленивая инициализация - при первом вызове
    private synchronized void init() {
        if (stub == null) {
            log.info("🔌 Подключение к gRPC серверу кухни {}:{} (при первом вызове)", kitchenHost, kitchenPort);
            channel = ManagedChannelBuilder
                    .forAddress(kitchenHost, kitchenPort)
                    .usePlaintext()
                    .build();
            stub = KitchenServiceGrpc.newBlockingStub(channel);
            log.info("✅ gRPC клиент кухни создан");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("✅ gRPC канал кухни закрыт");
            } catch (InterruptedException e) {
                log.error("Ошибка при остановке gRPC канала", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public OrderStatusResponse getOrderStatus(String orderId) {
        init(); // ✅ Инициализация при первом вызове
        log.debug("gRPC запрос статуса заказа: {}", orderId);

        GetOrderStatusRequest request = GetOrderStatusRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        return stub.getOrderStatus(request);
    }

    public EstimateCookingTimeResponse estimateCookingTime(List<String> menuItemIds) {
        init(); // ✅ Инициализация при первом вызове
        log.debug("gRPC запрос времени приготовления для {} блюд", menuItemIds.size());

        EstimateCookingTimeRequest request = EstimateCookingTimeRequest.newBuilder()
                .addAllMenuItemIds(menuItemIds)
                .build();

        return stub.estimateCookingTime(request);
    }
}