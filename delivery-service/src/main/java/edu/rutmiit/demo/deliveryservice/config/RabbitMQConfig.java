// config/RabbitMQConfig.java
package edu.rutmiit.demo.deliveryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    public static final String DELIVERY_QUEUE = "q.delivery.ready-orders";

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter(JsonMapper objectMapper) {
        return new JacksonJsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    @Bean
    public Binding deliveryBinding() {
        return BindingBuilder.bind(deliveryQueue())
                .to(new TopicExchange("ex.order"))
                .with("kitchen.cooking.completed");
    }
    @Bean
    public Binding deliveryOrderBinding() {
        return BindingBuilder.bind(deliveryQueue())
                .to(new TopicExchange("ex.order"))
                .with("kitchen.cooking.completed");  // Принимает kitchen.cooking.completed
    }
    @Bean
    public Binding deliveryCancelBinding() {
        return BindingBuilder.bind(deliveryQueue())
                .to(new TopicExchange("ex.order"))
                .with("order.cancelled");  // Принимает отмену
    }
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // НЕ устанавливаем MessageConverter!
        return factory;
    }
}