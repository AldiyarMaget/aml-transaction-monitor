package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.dto.TransactionEventMessage;
import kz.aml.transactionmonitor.dto.TransactionRequestDto;
import kz.aml.transactionmonitor.dto.TransactionResponseDto;
import kz.aml.transactionmonitor.exception.TransactionNotFoundException;
import kz.aml.transactionmonitor.model.Transaction;
import kz.aml.transactionmonitor.model.TransactionStatus;
import kz.aml.transactionmonitor.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionPublisher transactionPublisher;

    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto requestDto) {
        log.info("Creating new transaction: accountFrom='{}', accountTo='{}', amount={} {}",
                requestDto.getAccountFrom(), requestDto.getAccountTo(), requestDto.getAmount(), requestDto.getCurrency());

        Transaction transaction = Transaction.builder()
                .accountFrom(requestDto.getAccountFrom())
                .accountTo(requestDto.getAccountTo())
                .amount(requestDto.getAmount())
                .currency(requestDto.getCurrency().trim().toUpperCase())
                .status(TransactionStatus.PENDING)
                .riskNote("Pending AML automated check")
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved with initial status PENDING: id={}", savedTransaction.getId());

        TransactionEventMessage eventMessage = TransactionEventMessage.builder()
                .transactionId(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .currency(savedTransaction.getCurrency())
                .accountFrom(savedTransaction.getAccountFrom())
                .accountTo(savedTransaction.getAccountTo())
                .build();

        transactionPublisher.publishTransactionEvent(eventMessage);

        return mapToResponseDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionById(UUID id) {
        log.info("Fetching transaction by id={}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for id={}", id);
                    return new TransactionNotFoundException(id);
                });

        return mapToResponseDto(transaction);
    }

    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .accountFrom(transaction.getAccountFrom())
                .accountTo(transaction.getAccountTo())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .riskNote(transaction.getRiskNote())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
