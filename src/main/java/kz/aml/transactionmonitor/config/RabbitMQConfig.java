package kz.aml.transactionmonitor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${aml.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${aml.rabbitmq.queue}")
    private String queueName;

    @Value("${aml.rabbitmq.routingkey}")
    private String routingKey;

    @Bean
    public DirectExchange amlExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue amlQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding amlBinding(Queue amlQueue, DirectExchange amlExchange) {
        return BindingBuilder.bind(amlQueue).to(amlExchange).with(routingKey);
    }

    @Bean
    @SuppressWarnings("removal")
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
