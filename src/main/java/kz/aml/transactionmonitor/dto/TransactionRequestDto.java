package kz.aml.transactionmonitor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDto {

    @NotBlank(message = "Sender account (accountFrom) must not be blank")
    private String accountFrom;

    @NotBlank(message = "Recipient account (accountTo) must not be blank")
    private String accountTo;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Currency must not be blank")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (e.g. KZT, USD)")
    private String currency;
}
