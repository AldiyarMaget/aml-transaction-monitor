package kz.aml.transactionmonitor.dto;

import kz.aml.transactionmonitor.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {

    private UUID id;
    private String accountFrom;
    private String accountTo;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String riskNote;
    private Instant createdAt;
}
