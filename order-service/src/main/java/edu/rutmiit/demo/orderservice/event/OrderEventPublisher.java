package edu.rutmiit.demo.orderservice.event;

import edu.rutmiit.demo.darkkitchen.events.*;
import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;
import edu.rutmiit.demo.darkkitchenapi.dto.StatusHistoryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private static final String SOURCE = "order-service";

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderResponse order) {
        List<OrderEvent.OrderItem> items = order.getItems().stream()
                .map(item -> new OrderEvent.OrderItem(
                        item.menuItemId(), item.name(), item.quantity(), item.price()))
                .toList();

        OrderEvent.Created event = new OrderEvent.Created(
                order.getOrderId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                items,
                order.getNotes(),
                order.getCreatedAt()
        );

        send(RoutingKeys.ORDER_CREATED, event);
    }

    public void publishOrderStatusUpdated(OrderResponse order) {
        List<StatusHistoryEntry> history = order.getStatusHistory();
        StatusHistoryEntry lastEntry = history.get(history.size() - 1);

        OrderEvent.StatusUpdated event = new OrderEvent.StatusUpdated(
                order.getOrderId(),
                order.getStatus(),
                lastEntry.message(),
                lastEntry.timestamp()
        );

        send(RoutingKeys.ORDER_STATUS_UPDATED, event);
    }

    public void publishOrderCancelled(String orderId) {
        OrderEvent.Cancelled event = new OrderEvent.Cancelled(
                orderId,
                "Отменён клиентом",
                LocalDateTime.now().toString()
        );
        send(RoutingKeys.ORDER_CANCELLED, event);
    }

    private void send(String routingKey, OrderEvent event) {
        EventEnvelope<OrderEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);

        // Безопасное получение orderId для любого типа события
        String orderId = switch (event) {
            case OrderEvent.Created c -> c.orderId();
            case OrderEvent.StatusUpdated s -> s.orderId();
            case OrderEvent.Cancelled c -> c.orderId();
        };

        log.info("Публикация события {}: orderId={}", routingKey, orderId);
        rabbitTemplate.convertAndSend("ex.order", routingKey, envelope);
    }
}