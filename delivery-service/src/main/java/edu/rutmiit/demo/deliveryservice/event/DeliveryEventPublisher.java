package edu.rutmiit.demo.deliveryservice.event;

import edu.rutmiit.demo.darkkitchen.events.*;
import edu.rutmiit.demo.deliveryservice.model.Courier;  // ← Импорт публичного record

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DeliveryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventPublisher.class);
    private static final String SOURCE = "delivery-service";

    private final RabbitTemplate rabbitTemplate;

    public DeliveryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Теперь принимает публичный Courier из model пакета
    public void publishCourierAssigned(String orderId, Courier courier) {
        DeliveryEvent.CourierAssigned event = new DeliveryEvent.CourierAssigned(
                orderId, courier.id(), courier.name(), LocalDateTime.now()
        );
        send(RoutingKeys.DELIVERY_COURIER_ASSIGNED, event);
    }

    public void publishDeliveryStarted(String orderId, String courierId) {
        DeliveryEvent.DeliveryStarted event = new DeliveryEvent.DeliveryStarted(
                orderId, courierId, LocalDateTime.now()
        );
        send(RoutingKeys.DELIVERY_STARTED, event);
    }

    public void publishDelivered(String orderId, String courierId) {
        DeliveryEvent.Delivered event = new DeliveryEvent.Delivered(
                orderId, courierId, LocalDateTime.now()
        );
        send(RoutingKeys.DELIVERY_COMPLETED, event);
    }

    private void send(String routingKey, DeliveryEvent event) {
        EventEnvelope<DeliveryEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);

        log.info("Публикация события доставки: {}", routingKey);
        rabbitTemplate.convertAndSend("ex.order", routingKey, envelope);
    }
}