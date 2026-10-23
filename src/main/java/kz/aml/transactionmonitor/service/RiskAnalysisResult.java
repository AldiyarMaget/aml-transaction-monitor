package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.model.TransactionStatus;

public record RiskAnalysisResult(TransactionStatus status, String riskNote) {
}
