package edu.rutmiit.demo.notificationservice.listener;

import edu.rutmiit.demo.darkkitchen.events.*;
import edu.rutmiit.demo.notificationservice.config.RabbitMQConfig;
import edu.rutmiit.demo.notificationservice.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EventNotificationListener.class);

    private final NotificationWebSocketHandler webSocketHandler;
    private final JsonMapper jsonMapper;

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public EventNotificationListener(NotificationWebSocketHandler webSocketHandler,
                                     JsonMapper jsonMapper) {
        this.webSocketHandler = webSocketHandler;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE, messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();

            // Сначала парсим EventEnvelope<JsonNode> чтобы получить метаданные и тип payload
            JavaType envelopeType = jsonMapper.getTypeFactory()
                    .constructParametricType(EventEnvelope.class, tools.jackson.databind.JsonNode.class);
            EventEnvelope<tools.jackson.databind.JsonNode> envelope = jsonMapper.readValue(body, envelopeType);

            EventMetadata metadata = envelope.metadata();

            // Дедупликация
            if (!processedEventIds.add(metadata.eventId())) {
                log.warn("Дубликат уведомления пропущен: eventId={}", metadata.eventId());
                return;
            }

            // Парсим payload в зависимости от типа события
            Object payload = parsePayload(metadata.eventType(), envelope.payload());
            if (payload == null) {
                log.warn("Не удалось распарсить payload для события: {}", metadata.eventType());
                return;
            }

            // Формируем уведомление
            String title = buildTitle(metadata.eventType());
            String description = buildDescription(metadata.eventType(), payload);
            String icon = resolveIcon(metadata.eventType());
            String level = resolveLevel(metadata.eventType());

            String notificationJson = jsonMapper.writeValueAsString(
                    new NotificationPayload(
                            "NOTIFICATION",
                            metadata.eventId(),
                            metadata.eventType(),
                            title,
                            description,
                            icon,
                            level,
                            metadata.source(),
                            metadata.timestamp().toString(),
                            Instant.now().toString()
                    )
            );

            webSocketHandler.broadcast(notificationJson);

            log.info("[NOTIFY] {} | {} (клиентов: {})",
                    metadata.eventType(), description, webSocketHandler.getActiveConnectionCount());

        } catch (Exception e) {
            log.error("Ошибка обработки события для уведомлений: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    private Object parsePayload(String eventType, tools.jackson.databind.JsonNode payloadNode) {
        try {
            return switch (eventType) {
                case RoutingKeys.ORDER_CREATED ->
                        jsonMapper.treeToValue(payloadNode, OrderEvent.Created.class);
                case RoutingKeys.ORDER_STATUS_UPDATED ->
                        jsonMapper.treeToValue(payloadNode, OrderEvent.StatusUpdated.class);
                case RoutingKeys.ORDER_CANCELLED ->
                        jsonMapper.treeToValue(payloadNode, OrderEvent.Cancelled.class);
                case RoutingKeys.KITCHEN_COOKING_STARTED ->
                        jsonMapper.treeToValue(payloadNode, KitchenEvent.CookingStarted.class);
                case RoutingKeys.KITCHEN_COOKING_COMPLETED ->
                        jsonMapper.treeToValue(payloadNode, KitchenEvent.CookingCompleted.class);
                case RoutingKeys.DELIVERY_COURIER_ASSIGNED ->
                        jsonMapper.treeToValue(payloadNode, DeliveryEvent.CourierAssigned.class);
                case RoutingKeys.DELIVERY_STARTED ->
                        jsonMapper.treeToValue(payloadNode, DeliveryEvent.DeliveryStarted.class);
                case RoutingKeys.DELIVERY_COMPLETED ->
                        jsonMapper.treeToValue(payloadNode, DeliveryEvent.Delivered.class);
                default -> null;
            };
        } catch (Exception e) {
            log.error("Ошибка парсинга payload для события {}: {}", eventType, e.getMessage());
            return null;
        }
    }

    private String buildTitle(String eventType) {
        return switch (eventType) {
            case RoutingKeys.ORDER_CREATED -> "Новый заказ";
            case RoutingKeys.ORDER_STATUS_UPDATED -> "Статус заказа изменён";
            case RoutingKeys.ORDER_CANCELLED -> "Заказ отменён";
            case RoutingKeys.KITCHEN_COOKING_STARTED -> "Приготовление началось";
            case RoutingKeys.KITCHEN_COOKING_COMPLETED -> "Приготовление завершено";
            case RoutingKeys.DELIVERY_COURIER_ASSIGNED -> "Курьер назначен";
            case RoutingKeys.DELIVERY_STARTED -> "Доставка началась";
            case RoutingKeys.DELIVERY_COMPLETED -> "Заказ доставлен";
            default -> "Событие: " + eventType;
        };
    }

    private String buildDescription(String eventType, Object payload) {
        try {
            return switch (eventType) {
                case RoutingKeys.ORDER_CREATED -> {
                    OrderEvent.Created e = (OrderEvent.Created) payload;
                    String items = e.items().stream()
                            .map(i -> "%s x%d".formatted(i.name(), i.quantity()))
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("нет позиций");
                    yield "Заказ #%s от %s | Адрес: %s | Позиции: %s".formatted(
                            e.orderId(), e.customerName(), e.deliveryAddress(), items);
                }
                case RoutingKeys.ORDER_STATUS_UPDATED -> {
                    OrderEvent.StatusUpdated e = (OrderEvent.StatusUpdated) payload;
                    yield "Заказ #%s → %s: %s".formatted(e.orderId(), e.status(), e.message());
                }
                case RoutingKeys.ORDER_CANCELLED -> {
                    OrderEvent.Cancelled e = (OrderEvent.Cancelled) payload;
                    yield "Заказ #%s отменён: %s".formatted(e.orderId(), e.reason());
                }
                case RoutingKeys.KITCHEN_COOKING_STARTED -> {
                    KitchenEvent.CookingStarted e = (KitchenEvent.CookingStarted) payload;
                    yield "Заказ #%s — начали готовить".formatted(e.orderId());
                }
                case RoutingKeys.KITCHEN_COOKING_COMPLETED -> {
                    KitchenEvent.CookingCompleted e = (KitchenEvent.CookingCompleted) payload;
                    yield "Заказ #%s готов (готовили %dс)".formatted(e.orderId(), e.cookingTimeSeconds());
                }
                case RoutingKeys.DELIVERY_COURIER_ASSIGNED -> {
                    DeliveryEvent.CourierAssigned e = (DeliveryEvent.CourierAssigned) payload;
                    yield "Заказ #%s → курьер %s".formatted(e.orderId(), e.courierName());
                }
                case RoutingKeys.DELIVERY_STARTED -> {
                    DeliveryEvent.DeliveryStarted e = (DeliveryEvent.DeliveryStarted) payload;
                    yield "Заказ #%s — курьер в пути".formatted(e.orderId());
                }
                case RoutingKeys.DELIVERY_COMPLETED -> {
                    DeliveryEvent.Delivered e = (DeliveryEvent.Delivered) payload;
                    yield "Заказ #%s доставлен!".formatted(e.orderId());
                }
                default -> "Неизвестное событие: " + eventType;
            };
        } catch (Exception e) {
            return "Событие " + eventType + " (ошибка формирования описания)";
        }
    }

    private String resolveIcon(String eventType) {
        return switch (eventType) {
            case RoutingKeys.ORDER_CREATED -> "order-create";
            case RoutingKeys.ORDER_STATUS_UPDATED -> "order-status";
            case RoutingKeys.ORDER_CANCELLED -> "order-cancel";
            case RoutingKeys.KITCHEN_COOKING_STARTED -> "cooking";
            case RoutingKeys.KITCHEN_COOKING_COMPLETED -> "cooking-done";
            case RoutingKeys.DELIVERY_COURIER_ASSIGNED -> "courier";
            case RoutingKeys.DELIVERY_STARTED -> "delivery";
            case RoutingKeys.DELIVERY_COMPLETED -> "delivery-done";
            default -> "bell";
        };
    }

    private String resolveLevel(String eventType) {
        return switch (eventType) {
            case RoutingKeys.ORDER_CANCELLED -> "warning";
            case RoutingKeys.DELIVERY_COMPLETED -> "success";
            default -> "info";
        };
    }

    record NotificationPayload(
            String type,
            String eventId,
            String eventType,
            String title,
            String description,
            String icon,
            String level,
            String source,
            String eventTimestamp,
            String receivedAt
    ) {}
}