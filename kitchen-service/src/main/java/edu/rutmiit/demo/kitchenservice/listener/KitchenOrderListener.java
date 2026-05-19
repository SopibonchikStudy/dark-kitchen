package edu.rutmiit.demo.kitchenservice.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rutmiit.demo.darkkitchen.events.*;
import edu.rutmiit.demo.kitchenservice.event.KitchenEventPublisher;
import edu.rutmiit.demo.kitchenservice.grpc.KitchenGrpcService;  // ← ДОБАВИТЬ ИМПОРТ
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KitchenOrderListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenOrderListener.class);

    private final KitchenEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final KitchenGrpcService kitchenGrpcService;  // ← ДОБАВИТЬ ПОЛЕ

    // ← ОБНОВИТЬ КОНСТРУКТОР
    public KitchenOrderListener(KitchenEventPublisher eventPublisher,
                                ObjectMapper objectMapper,
                                KitchenGrpcService kitchenGrpcService) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.kitchenGrpcService = kitchenGrpcService;
    }

    @RabbitListener(queues = "q.kitchen.orders", messageConverter = "")
    public void handleNewOrder(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = objectMapper.readTree(body);
            JsonNode payloadNode = root.get("payload");

            OrderEvent.Created order = objectMapper.treeToValue(
                    payloadNode, OrderEvent.Created.class);

            log.info("Получен новый заказ: orderId={}, блюд: {}, клиент: {}",
                    order.orderId(), order.items().size(), order.customerName());

            processOrder(order);

        } catch (Exception e) {
            log.error("Ошибка обработки заказа: {}", e.getMessage(), e);
        }
    }

    private void processOrder(OrderEvent.Created order) {
        try {
            // ← ОБНОВЛЯЕМ СТАТУС ДЛЯ gRPC
            int cookingTime = estimateCookingTime(order.items());
            kitchenGrpcService.updateOrderStatus(
                    order.orderId(), "COOKING", "Заказ готовится", cookingTime);

            eventPublisher.publishCookingStarted(order.orderId());

            log.info("🍳 Готовим заказ {}...", order.orderId());
            int delay = 30;
            Thread.sleep(delay * 1000); // 30 секунд готовки

            // ← ОБНОВЛЯЕМ СТАТУС ДЛЯ gRPC
            kitchenGrpcService.updateOrderStatus(
                    order.orderId(), "READY", "Заказ готов", 0);

            eventPublisher.publishCookingCompleted(order.orderId(), 5);

            log.info("✅ Заказ {} готов!", order.orderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Приготовление прервано: orderId={}", order.orderId());

            // ← ОБНОВЛЯЕМ СТАТУС ОШИБКИ ДЛЯ gRPC
            kitchenGrpcService.updateOrderStatus(
                    order.orderId(), "ERROR", "Ошибка приготовления", 0);
        }
    }

    // ← ДОБАВИТЬ МЕТОД ОЦЕНКИ ВРЕМЕНИ
    private int estimateCookingTime(List<OrderEvent.OrderItem> items) {
        // Можно использовать те же значения что в KitchenGrpcService
        return items.size() * 300; // Примерно 5 минут на блюдо
    }
}