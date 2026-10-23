package kz.aml.transactionmonitor.exception;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(UUID id) {
        super(String.format("Transaction with ID '%s' not found", id));
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
