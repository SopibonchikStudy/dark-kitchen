// listener/NotificationListener.java
package edu.rutmiit.demo.notificationservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final ObjectMapper jsonMapper;
    private final ConcurrentLinkedDeque<NotificationRecord> notifications = new ConcurrentLinkedDeque<>();

    public NotificationListener(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.notification.events")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            String eventType = root.get("metadata").get("eventType").asText();
            String source = root.get("metadata").get("source").asText();
            String eventId = root.get("metadata").get("eventId").asText();

            JsonNode payload = root.get("payload");
            String orderId = extractOrderId(payload);

            // Формируем уведомление
            String notificationMessage = formatNotification(eventType, payload);

            NotificationRecord record = new NotificationRecord(
                    eventId, eventType, source, orderId, notificationMessage, LocalDateTime.now()
            );

            notifications.addFirst(record);

            // Имитация отправки уведомления
            log.info("📱 УВЕДОМЛЕНИЕ: {}", notificationMessage);

            // Ограничиваем историю
            if (notifications.size() > 100) {
                notifications.pollLast();
            }

        } catch (Exception e) {
            log.error("Ошибка обработки уведомления: {}", e.getMessage(), e);
        }
    }

    private String extractOrderId(JsonNode payload) {
        if (payload.has("orderId")) {
            return payload.get("orderId").asText();
        }
        return "UNKNOWN";
    }

    private String formatNotification(String eventType, JsonNode payload) {
        String orderId = extractOrderId(payload);

        return switch (eventType) {
            case "order.created" ->
                    String.format("Новый заказ %s от %s", orderId, payload.get("customerName").asText());
            case "kitchen.cooking.started" ->
                    String.format("Заказ %s начали готовить", orderId);
            case "kitchen.cooking.completed" ->
                    String.format("Заказ %s готов к доставке", orderId);
            case "delivery.courier.assigned" ->
                    String.format("Курьер %s назначен на заказ %s",
                            payload.get("courierName").asText(), orderId);
            case "delivery.started" ->
                    String.format("Заказ %s в пути", orderId);
            case "delivery.completed" ->
                    String.format("Заказ %s доставлен!", orderId);
            case "order.status.updated" ->
                    String.format("Заказ %s: %s", orderId, payload.get("message").asText());
            default ->
                    String.format("Событие по заказу %s: %s", orderId, eventType);
        };
    }

    public List<NotificationRecord> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public record NotificationRecord(
            String eventId,
            String eventType,
            String source,
            String orderId,
            String message,
            LocalDateTime timestamp
    ) {}
}