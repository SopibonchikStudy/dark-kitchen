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
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KitchenOrderListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenOrderListener.class);
    private final ConcurrentHashMap<String, Thread> activeOrders = new ConcurrentHashMap<>();
    private final KitchenEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final KitchenGrpcService kitchenGrpcService;

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
            JsonNode metaNode = root.get("metadata");
            String eventType = metaNode.get("eventType").asText();

            if ("order.cancelled".equals(eventType)) {
                JsonNode payloadNode = root.get("payload");
                String orderId = payloadNode.get("orderId").asText();
                log.info("❌ Заказ {} отменён, приготовление не требуется", orderId);
                return;
            }

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
        Thread cookingThread = Thread.currentThread();
        activeOrders.put(order.orderId(), cookingThread);

        try {
            kitchenGrpcService.updateOrderStatus(order.orderId(), "COOKING", "Готовится", 300);
            eventPublisher.publishCookingStarted(order.orderId());
            int delay = 30;
            Thread.sleep(delay * 1000);

            // Проверяем, не отменили ли заказ
            if (!activeOrders.containsKey(order.orderId())) {
                log.info("❌ Заказ {} был отменён во время приготовления", order.orderId());
                return;
            }

            kitchenGrpcService.updateOrderStatus(order.orderId(), "READY", "Готов", 0);
            eventPublisher.publishCookingCompleted(order.orderId(), 5);

        } catch (InterruptedException e) {
            log.info("❌ Приготовление заказа {} прервано", order.orderId());
            Thread.currentThread().interrupt();
        } finally {
            activeOrders.remove(order.orderId());
        }
    }

    // ← ДОБАВИТЬ МЕТОД ОЦЕНКИ ВРЕМЕНИ
    private int estimateCookingTime(List<OrderEvent.OrderItem> items) {
        // Можно использовать те же значения что в KitchenGrpcService
        return items.size() * 300; // Примерно 5 минут на блюдо
    }

    private void cancelCooking(String orderId) {
        Thread cookingThread = activeOrders.remove(orderId);
        if (cookingThread != null) {
            cookingThread.interrupt();  // Прерываем поток приготовления
            log.info("❌ Приготовление заказа {} прервано", orderId);
        }
    }
}