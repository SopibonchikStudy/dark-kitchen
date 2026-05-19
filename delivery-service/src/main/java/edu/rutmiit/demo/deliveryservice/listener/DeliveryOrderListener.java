package edu.rutmiit.demo.deliveryservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import edu.rutmiit.demo.darkkitchen.events.KitchenEvent;
import edu.rutmiit.demo.deliveryservice.event.DeliveryEventPublisher;
import edu.rutmiit.demo.deliveryservice.model.Courier;  // ← Импорт публичного record

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
    private final ObjectMapper jsonMapper;
    private final Random random = new Random();

    // Пул курьеров (использует публичный record Courier)
    private final List<Courier> couriers = List.of(
            new Courier("COUR-001", "Алексей Смирнов", "+79001112233"),
            new Courier("COUR-002", "Дмитрий Иванов", "+79002223344"),
            new Courier("COUR-003", "Сергей Петров", "+79003334455")
    );

    public DeliveryOrderListener(DeliveryEventPublisher eventPublisher, ObjectMapper jsonMapper) {
        this.eventPublisher = eventPublisher;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.delivery.ready-orders", messageConverter = "")
    public void handleReadyOrder(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);
            JsonNode payloadNode = root.get("payload");

            KitchenEvent.CookingCompleted event = jsonMapper.treeToValue(
                    payloadNode, KitchenEvent.CookingCompleted.class);

            log.info("Заказ готов к доставке: orderId={}", event.orderId());

            // Назначаем курьера
            assignCourier(event.orderId());

        } catch (Exception e) {
            log.error("Ошибка обработки готового заказа: {}", e.getMessage(), e);
        }
    }

    private void assignCourier(String orderId) {
        Courier courier = couriers.get(random.nextInt(couriers.size()));

        log.info("🚴 Курьер {} назначен на заказ {}", courier.name(), orderId);
        eventPublisher.publishCourierAssigned(orderId, courier);

        // Эмулируем доставку
        simulateDelivery(orderId, courier);
    }

    private void simulateDelivery(String orderId, Courier courier) {
        try {
            // Начинаем доставку
            eventPublisher.publishDeliveryStarted(orderId, courier.id());

            log.info("📦 Доставляем заказ {}...", orderId);
            Thread.sleep(3000); // эмуляция доставки

            // Завершаем доставку
            eventPublisher.publishDelivered(orderId, courier.id());

            log.info("✅ Заказ {} доставлен!", orderId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Доставка прервана: orderId={}", orderId);
        }
    }
}