package edu.rutmiit.demo.deliveryservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import edu.rutmiit.demo.darkkitchen.events.KitchenEvent;
import edu.rutmiit.demo.deliveryservice.event.DeliveryEventPublisher;
import edu.rutmiit.demo.deliveryservice.grpc.DeliveryGrpcService;
import edu.rutmiit.demo.deliveryservice.model.Courier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class DeliveryOrderListener {

    private static final Logger log = LoggerFactory.getLogger(DeliveryOrderListener.class);

    private final DeliveryEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final DeliveryGrpcService deliveryGrpcService;  // ← ДОЛЖНО БЫТЬ
    private final Random random = new Random();

    private final List<Courier> couriers = List.of(
            new Courier("COUR-001", "Алексей Смирнов", "+79001112233"),
            new Courier("COUR-002", "Дмитрий Иванов", "+79002223344"),
            new Courier("COUR-003", "Сергей Петров", "+79003334455")
    );

    // ← УБЕДИТЕСЬ ЧТО КОНСТРУКТОР ПРИНИМАЕТ DeliveryGrpcService
    public DeliveryOrderListener(DeliveryEventPublisher eventPublisher,
                                 ObjectMapper objectMapper,
                                 DeliveryGrpcService deliveryGrpcService) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.deliveryGrpcService = deliveryGrpcService;
    }

    @RabbitListener(queues = "q.delivery.ready-orders", messageConverter = "")
    public void handleReadyOrder(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = objectMapper.readTree(body);
            JsonNode payloadNode = root.get("payload");

            KitchenEvent.CookingCompleted event = objectMapper.treeToValue(
                    payloadNode, KitchenEvent.CookingCompleted.class);

            log.info("Заказ готов к доставке: orderId={}", event.orderId());

            assignCourier(event.orderId());

        } catch (Exception e) {
            log.error("Ошибка обработки готового заказа: {}", e.getMessage(), e);
        }
    }

    private void assignCourier(String orderId) {
        Courier courier = couriers.get(random.nextInt(couriers.size()));

        // ← СОХРАНЯЕМ ДЛЯ gRPC
        deliveryGrpcService.updateDeliveryStatus(orderId, "ASSIGNED", courier, 15);

        log.info("🚴 Курьер {} назначен на заказ {}", courier.name(), orderId);
        eventPublisher.publishCourierAssigned(orderId, courier);

        simulateDelivery(orderId, courier);
    }

    private void simulateDelivery(String orderId, Courier courier) {
        try {
            // ← ОБНОВЛЯЕМ ДЛЯ gRPC
            deliveryGrpcService.updateDeliveryStatus(orderId, "DELIVERING", courier, 10);

            eventPublisher.publishDeliveryStarted(orderId, courier.id());

            log.info("📦 Доставляем заказ {}...", orderId);
            int delay = 30;
            Thread.sleep(delay * 1000);

            // ← ОБНОВЛЯЕМ ДЛЯ gRPC
            deliveryGrpcService.updateDeliveryStatus(orderId, "DELIVERED", courier, 0);

            eventPublisher.publishDelivered(orderId, courier.id());

            log.info("✅ Заказ {} доставлен!", orderId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Доставка прервана: orderId={}", orderId);

            // ← ОБНОВЛЯЕМ ДЛЯ gRPC
            deliveryGrpcService.updateDeliveryStatus(orderId, "ERROR", courier, 0);
        }
    }
}