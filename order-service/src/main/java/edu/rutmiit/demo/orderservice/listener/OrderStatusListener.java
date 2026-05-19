// listener/OrderStatusListener.java
package edu.rutmiit.demo.orderservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import edu.rutmiit.demo.darkkitchen.events.*;
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
    private final ObjectMapper jsonMapper;

    public OrderStatusListener(OrderService orderService, ObjectMapper jsonMapper) {
        this.orderService = orderService;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.order.status", messageConverter = "")
    public void handleStatusUpdate(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);
            JsonNode payloadNode = root.get("payload");

            String eventType = root.get("metadata").get("eventType").asText();

            switch (eventType) {
                case "kitchen.cooking.started" -> handleCookingStarted(payloadNode);
                case "kitchen.cooking.completed" -> handleCookingCompleted(payloadNode);
                case "delivery.courier.assigned" -> handleCourierAssigned(payloadNode);
                case "delivery.started" -> handleDeliveryStarted(payloadNode);
                case "delivery.completed" -> handleDeliveryCompleted(payloadNode);
                default -> log.warn("Неизвестный тип события: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки сообщения: {}", e.getMessage(), e);
        }
    }

    private void handleCookingStarted(JsonNode payload) {
        String orderId = payload.get("orderId").asText();
        orderService.updateStatus(orderId, "COOKING", "Заказ готовится");
        log.info("Приготовление начато: orderId={}", orderId);
    }

    private void handleCookingCompleted(JsonNode payload) {
        String orderId = payload.get("orderId").asText();
        orderService.updateStatus(orderId, "READY", "Заказ готов к доставке");
        log.info("Приготовление завершено: orderId={}", orderId);
    }

    private void handleCourierAssigned(JsonNode payload) {
        String orderId = payload.get("orderId").asText();
        CourierInfo courier = new CourierInfo(
                payload.get("courierId").asText(),
                payload.get("courierName").asText(),
                null
        );
        orderService.updateOrderWithCourier(orderId, courier, "ASSIGNED", "Курьер назначен");
        log.info("Курьер назначен: orderId={}, courierId={}", orderId, courier.courierId());
    }

    private void handleDeliveryStarted(JsonNode payload) {
        String orderId = payload.get("orderId").asText();
        orderService.updateStatus(orderId, "DELIVERING", "Заказ в пути");
        log.info("Доставка начата: orderId={}", orderId);
    }

    private void handleDeliveryCompleted(JsonNode payload) {
        String orderId = payload.get("orderId").asText();
        orderService.updateStatus(orderId, "DELIVERED", "Заказ доставлен");
        log.info("Заказ доставлен: orderId={}", orderId);
    }
}