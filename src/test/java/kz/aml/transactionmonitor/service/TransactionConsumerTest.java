package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.dto.TransactionEventMessage;
import kz.aml.transactionmonitor.model.Transaction;
import kz.aml.transactionmonitor.model.TransactionStatus;
import kz.aml.transactionmonitor.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionConsumerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AmlFraudDetectionEngine fraudDetectionEngine;

    @InjectMocks
    private TransactionConsumer transactionConsumer;

    @Test
    @DisplayName("consumeTransactionEvent should evaluate risk and update transaction status to SUSPICIOUS")
    void testConsumeTransactionEventSuspicious() {
        UUID txId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("8000000.00");
        String currency = "KZT";

        TransactionEventMessage event = TransactionEventMessage.builder()
                .transactionId(txId)
                .amount(amount)
                .currency(currency)
                .accountFrom("KZ123")
                .accountTo("KZ456")
                .build();

        Transaction existing = Transaction.builder()
                .id(txId)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .currency(currency)
                .accountFrom("KZ123")
                .accountTo("KZ456")
                .build();

        when(fraudDetectionEngine.evaluateRisk(amount, currency))
                .thenReturn(new RiskAnalysisResult(TransactionStatus.SUSPICIOUS, "High amount threshold exceeded"));
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        transactionConsumer.consumeTransactionEvent(event);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();
        assertEquals(TransactionStatus.SUSPICIOUS, saved.getStatus());
        assertEquals("High amount threshold exceeded", saved.getRiskNote());
    }
}
