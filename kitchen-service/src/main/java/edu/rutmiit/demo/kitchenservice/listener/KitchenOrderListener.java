// listener/KitchenOrderListener.java
package edu.rutmiit.demo.kitchenservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import edu.rutmiit.demo.darkkitchen.events.*;
import edu.rutmiit.demo.kitchenservice.event.KitchenEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class KitchenOrderListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenOrderListener.class);

    private final KitchenEventPublisher eventPublisher;
    private final ObjectMapper jsonMapper;

    public KitchenOrderListener(KitchenEventPublisher eventPublisher, ObjectMapper jsonMapper) {
        this.eventPublisher = eventPublisher;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.kitchen.orders", messageConverter = "")
    public void handleNewOrder(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);
            JsonNode payloadNode = root.get("payload");

            OrderEvent.Created order = jsonMapper.treeToValue(payloadNode, OrderEvent.Created.class);

            log.info("Получен новый заказ: orderId={}, блюд: {}, клиент: {}",
                    order.orderId(), order.items().size(), order.customerName());

            // Эмулируем приготовление
            processOrder(order);

        } catch (Exception e) {
            log.error("Ошибка обработки заказа: {}", e.getMessage(), e);
        }
    }

    private void processOrder(OrderEvent.Created order) {
        try {
            // Начинаем приготовление
            eventPublisher.publishCookingStarted(order.orderId());

            log.info("🍳 Готовим заказ {}...", order.orderId());

            // Эмуляция времени приготовления
            int cookingTime = 5; // секунд
            Thread.sleep(cookingTime * 1000);

            // Завершаем приготовление
            eventPublisher.publishCookingCompleted(order.orderId(), cookingTime);

            log.info("✅ Заказ {} готов!", order.orderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Приготовление прервано: orderId={}", order.orderId());
        }
    }
}