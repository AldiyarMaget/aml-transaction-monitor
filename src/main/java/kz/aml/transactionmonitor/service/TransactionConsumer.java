package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.dto.TransactionEventMessage;
import kz.aml.transactionmonitor.model.Transaction;
import kz.aml.transactionmonitor.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionRepository transactionRepository;
    private final AmlFraudDetectionEngine fraudDetectionEngine;

    @RabbitListener(queues = "${aml.rabbitmq.queue}")
    @Transactional
    public void consumeTransactionEvent(TransactionEventMessage event) {
        log.info("Received transaction event from queue: transactionId={}, amount={} {}",
                event.getTransactionId(), event.getAmount(), event.getCurrency());

        RiskAnalysisResult analysisResult = fraudDetectionEngine.evaluateRisk(
                event.getAmount(),
                event.getCurrency()
        );

        Optional<Transaction> optionalTransaction = transactionRepository.findById(event.getTransactionId());
        if (optionalTransaction.isEmpty()) {
            log.error("Transaction not found in database for transactionId={}", event.getTransactionId());
            return;
        }

        Transaction transaction = optionalTransaction.get();
        transaction.setStatus(analysisResult.status());
        transaction.setRiskNote(analysisResult.riskNote());

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction updated after AML check: transactionId={}, status={}, riskNote='{}'",
                updated.getId(), updated.getStatus(), updated.getRiskNote());
    }
}
