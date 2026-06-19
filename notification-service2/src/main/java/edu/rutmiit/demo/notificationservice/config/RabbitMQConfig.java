package edu.rutmiit.demo.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Конфигурация RabbitMQ для notification-service.
 *
 * Сервис чистый consumer и слушает все события (binding "#"),
 * чтобы отправлять их подключённым WebSocket-клиентам.
 *
 */
@Configuration
public class RabbitMQConfig {

    // Имя exchange должно совпадать с тем, куда публикуют другие сервисы
    public static final String EVENTS_EXCHANGE = "ex.order"; // или "darkkitchen.events" - уточните в других сервисах
    public static final String NOTIFICATIONS_QUEUE = "q.notifications.all";
    public static final String NOTIFICATIONS_DLQ = "q.notifications.all.dlq";
    public static final String ALL_EVENTS = "#";

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        factory.setDefaultRequeueRejected(false); // при ошибке → DLQ
        return factory;
    }

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(EVENTS_EXCHANGE + ".dlx")
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder
                .durable(NOTIFICATIONS_QUEUE)
                .deadLetterExchange(EVENTS_EXCHANGE + ".dlx")
                .deadLetterRoutingKey(NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue notificationsDlq() {
        return QueueBuilder.durable(NOTIFICATIONS_DLQ).build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(notificationsQueue)
                .to(eventsExchange)
                .with(ALL_EVENTS); // "#" — все события
    }

    @Bean
    public Binding notificationsDlqBinding(Queue notificationsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(notificationsDlq)
                .to(deadLetterExchange)
                .with(NOTIFICATIONS_DLQ);
    }
}