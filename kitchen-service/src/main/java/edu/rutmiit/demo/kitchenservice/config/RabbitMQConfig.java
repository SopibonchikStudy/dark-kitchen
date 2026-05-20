// config/RabbitMQConfig.java
package edu.rutmiit.demo.kitchenservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String KITCHEN_QUEUE = "q.kitchen.orders";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public Queue kitchenQueue() {
        return new Queue(KITCHEN_QUEUE, true);
    }

    @Bean
    public Binding kitchenBinding() {
        return BindingBuilder.bind(kitchenQueue())
                .to(new TopicExchange("ex.order"))
                .with("order.created");
    }
    @Bean
    public Binding kitchenCancelBinding() {
        return BindingBuilder.bind(kitchenQueue())
                .to(new TopicExchange("ex.order"))
                .with("order.cancelled");  // Только отмены
    }
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(2);  // ← МИНИМУМ 2 потока
        factory.setMaxConcurrentConsumers(5);  // ← МАКСИМУМ 5 потоков
        return factory;
    }
}