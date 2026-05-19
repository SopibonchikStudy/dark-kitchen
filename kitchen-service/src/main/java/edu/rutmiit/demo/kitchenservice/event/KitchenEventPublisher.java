// event/KitchenEventPublisher.java
package edu.rutmiit.demo.kitchenservice.event;

import edu.rutmiit.demo.darkkitchen.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class KitchenEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KitchenEventPublisher.class);
    private static final String SOURCE = "kitchen-service";

    private final RabbitTemplate rabbitTemplate;

    public KitchenEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCookingStarted(String orderId) {
        KitchenEvent.CookingStarted event = new KitchenEvent.CookingStarted(
                orderId, LocalDateTime.now()
        );
        send(RoutingKeys.KITCHEN_COOKING_STARTED, event);
    }

    public void publishCookingCompleted(String orderId, int cookingTimeSeconds) {
        KitchenEvent.CookingCompleted event = new KitchenEvent.CookingCompleted(
                orderId, LocalDateTime.now(), cookingTimeSeconds
        );
        send(RoutingKeys.KITCHEN_COOKING_COMPLETED, event);
    }

    private void send(String routingKey, KitchenEvent event) {
        EventEnvelope<KitchenEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);

        log.info("Публикация кухонного события: {}", routingKey);
        rabbitTemplate.convertAndSend("ex.order", routingKey, envelope);
    }
}