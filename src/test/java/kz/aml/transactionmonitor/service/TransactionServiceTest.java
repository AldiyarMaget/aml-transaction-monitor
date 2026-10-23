package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.dto.TransactionEventMessage;
import kz.aml.transactionmonitor.dto.TransactionRequestDto;
import kz.aml.transactionmonitor.dto.TransactionResponseDto;
import kz.aml.transactionmonitor.exception.TransactionNotFoundException;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionPublisher transactionPublisher;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("createTransaction should save entity with PENDING status and publish event")
    void testCreateTransaction() {
        TransactionRequestDto request = TransactionRequestDto.builder()
                .accountFrom("KZ123456789")
                .accountTo("KZ987654321")
                .amount(new BigDecimal("1500000.00"))
                .currency("KZT")
                .build();

        UUID generatedId = UUID.randomUUID();
        Instant now = Instant.now();

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(generatedId);
            t.setCreatedAt(now);
            return t;
        });

        TransactionResponseDto response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(generatedId, response.getId());
        assertEquals("KZ123456789", response.getAccountFrom());
        assertEquals("KZ987654321", response.getAccountTo());
        assertEquals(new BigDecimal("1500000.00"), response.getAmount());
        assertEquals("KZT", response.getCurrency());
        assertEquals(TransactionStatus.PENDING, response.getStatus());

        ArgumentCaptor<TransactionEventMessage> messageCaptor = ArgumentCaptor.forClass(TransactionEventMessage.class);
        verify(transactionPublisher).publishTransactionEvent(messageCaptor.capture());

        TransactionEventMessage publishedEvent = messageCaptor.getValue();
        assertEquals(generatedId, publishedEvent.getTransactionId());
        assertEquals(new BigDecimal("1500000.00"), publishedEvent.getAmount());
        assertEquals("KZT", publishedEvent.getCurrency());
    }

    @Test
    @DisplayName("getTransactionById should return DTO when entity exists")
    void testGetTransactionByIdFound() {
        UUID id = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(id)
                .accountFrom("KZ111")
                .accountTo("KZ222")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .status(TransactionStatus.APPROVED)
                .riskNote("Automated check passed")
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        TransactionResponseDto response = transactionService.getTransactionById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(TransactionStatus.APPROVED, response.getStatus());
        assertEquals("Automated check passed", response.getRiskNote());
    }

    @Test
    @DisplayName("getTransactionById should throw TransactionNotFoundException when not found")
    void testGetTransactionByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById(id));
    }
}
