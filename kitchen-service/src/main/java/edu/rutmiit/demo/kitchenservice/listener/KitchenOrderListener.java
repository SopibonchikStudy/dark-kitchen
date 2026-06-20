package edu.rutmiit.demo.kitchenservice.listener;

import edu.rutmiit.demo.darkkitchen.events.*;
import edu.rutmiit.demo.kitchenservice.event.KitchenEventPublisher;
import edu.rutmiit.demo.kitchenservice.grpc.KitchenGrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class KitchenOrderListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenOrderListener.class);
    private final ConcurrentHashMap<String, Thread> activeOrders = new ConcurrentHashMap<>();
    private final KitchenEventPublisher eventPublisher;
    private final JsonMapper objectMapper;
    private final KitchenGrpcService kitchenGrpcService;

    public KitchenOrderListener(KitchenEventPublisher eventPublisher,
                                JsonMapper objectMapper,
                                KitchenGrpcService kitchenGrpcService) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.kitchenGrpcService = kitchenGrpcService;
    }

    @RabbitListener(queues = "q.kitchen.orders")
    public void handleMessage(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = objectMapper.readTree(body);
            JsonNode metaNode = root.get("metadata");
            String eventType = metaNode.get("eventType").asText();

            if ("order.cancelled".equals(eventType)) {
                JsonNode payloadNode = root.get("payload");
                String orderId = payloadNode.get("orderId").asText();
                cancelCooking(orderId);
                return;
            }

            if ("order.created".equals(eventType)) {
                JsonNode payloadNode = root.get("payload");
                OrderEvent.Created order = objectMapper.treeToValue(
                        payloadNode, OrderEvent.Created.class);

                log.info("Получен новый заказ: orderId={}, блюд: {}, клиент: {}",
                        order.orderId(), order.items().size(), order.customerName());

                processOrder(order);
            }

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения: {}", e.getMessage(), e);
        }
    }

    private void cancelCooking(String orderId) {
        Thread cookingThread = activeOrders.remove(orderId);
        if (cookingThread != null) {
            cookingThread.interrupt();
            log.info("❌ Приготовление заказа {} прервано", orderId);
        } else {
            log.info("❌ Заказ {} отменён (приготовление ещё не начато)", orderId);
        }
        kitchenGrpcService.updateOrderStatus(orderId, "CANCELLED", "Отменён", 0);
    }

    private void processOrder(OrderEvent.Created order) {
        String orderId = order.orderId();

        Thread cookingThread = new Thread(() -> {
            try {
                kitchenGrpcService.updateOrderStatus(orderId, "COOKING", "Готовится", 300);
                eventPublisher.publishCookingStarted(orderId);

                log.info("🍳 Готовим заказ {}...", orderId);
                int delay = 30;
                Thread.sleep(delay*1000);

                if (!activeOrders.containsKey(orderId)) {
                    log.info("❌ Заказ {} был отменён во время приготовления", orderId);
                    return;
                }

                kitchenGrpcService.updateOrderStatus(orderId, "READY", "Готов", 0);
                eventPublisher.publishCookingCompleted(orderId, 5);

                log.info("✅ Заказ {} готов!", orderId);

            } catch (InterruptedException e) {
                log.info("❌ Приготовление заказа {} прервано", orderId);
                Thread.currentThread().interrupt();
            } finally {
                activeOrders.remove(orderId);
            }
        });

        activeOrders.put(orderId, cookingThread);
        cookingThread.start();
    }
}