package kz.aml.transactionmonitor.controller;

import jakarta.validation.Valid;
import kz.aml.transactionmonitor.dto.TransactionRequestDto;
import kz.aml.transactionmonitor.dto.TransactionResponseDto;
import kz.aml.transactionmonitor.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("REST POST /api/v1/transactions called for accountFrom='{}', accountTo='{}'",
                requestDto.getAccountFrom(), requestDto.getAccountTo());
        TransactionResponseDto response = transactionService.createTransaction(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable UUID id) {
        log.info("REST GET /api/v1/transactions/{} called", id);
        TransactionResponseDto response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }
}
