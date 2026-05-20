package edu.rutmiit.demo.orderservice.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rutmiit.demo.darkkitchenapi.dto.CourierInfo;
import edu.rutmiit.demo.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusListener {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusListener.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderStatusListener(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "q.order.status")
    public void handleStatusUpdate(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = objectMapper.readTree(body);
            JsonNode payloadNode = root.get("payload");
            String eventType = root.get("metadata").get("eventType").asText();

            switch (eventType) {
                case "kitchen.cooking.started" -> {
                    String orderId = payloadNode.get("orderId").asText();
                    orderService.updateStatus(orderId, "COOKING", "Заказ готовится");
                    log.info("🍳 Приготовление начато: orderId={}", orderId);
                }
                case "kitchen.cooking.completed" -> {
                    String orderId = payloadNode.get("orderId").asText();
                    orderService.updateStatus(orderId, "READY", "Заказ готов к доставке");
                    log.info("✅ Приготовление завершено: orderId={}", orderId);
                }
                case "delivery.courier.assigned" -> {
                    String orderId = payloadNode.get("orderId").asText();
                    CourierInfo courier = new CourierInfo(
                            payloadNode.get("courierId").asText(),
                            payloadNode.get("courierName").asText(),
                            null
                    );
                    orderService.updateOrderWithCourier(orderId, courier, "ASSIGNED", "Курьер назначен");
                    log.info("🚴 Курьер назначен: orderId={}, courier={}", orderId, courier.courierName());
                }
                case "delivery.started" -> {
                    String orderId = payloadNode.get("orderId").asText();
                    orderService.updateStatus(orderId, "DELIVERING", "Заказ в пути");
                    log.info("📦 Доставка начата: orderId={}", orderId);
                }
                case "delivery.completed" -> {
                    String orderId = payloadNode.get("orderId").asText();
                    orderService.updateStatus(orderId, "DELIVERED", "Заказ доставлен");
                    log.info("🏁 Заказ доставлен: orderId={}", orderId);
                }
                case "order.cancelled" -> {
                    String orderId = payloadNode.get("orderId").asText();
                    orderService.updateStatus(orderId, "CANCELLED", "Заказ отменён");
                    log.info("❌ Заказ отменён: orderId={}", orderId);
                }
                default -> log.warn("⚠️ Неизвестный тип события: {}", eventType);
            }
        } catch (Exception e) {
            log.error("❌ Ошибка обработки статуса: {}", e.getMessage(), e);
        }
    }
}