package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.dto.TransactionEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public TransactionPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${aml.rabbitmq.exchange}") String exchange,
            @Value("${aml.rabbitmq.routingkey}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishTransactionEvent(TransactionEventMessage message) {
        log.info("Publishing transaction event to exchange='{}' with routingKey='{}' for transactionId={}",
                exchange, routingKey, message.getTransactionId());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("Successfully published transaction event for transactionId={}", message.getTransactionId());
    }
}
