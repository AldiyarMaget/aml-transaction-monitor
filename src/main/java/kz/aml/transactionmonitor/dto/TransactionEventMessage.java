package kz.aml.transactionmonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEventMessage implements Serializable {

    private UUID transactionId;
    private BigDecimal amount;
    private String currency;
    private String accountFrom;
    private String accountTo;
}
