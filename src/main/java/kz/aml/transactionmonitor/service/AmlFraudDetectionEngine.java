package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.model.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
public class AmlFraudDetectionEngine {

    private static final BigDecimal KZT_THRESHOLD = new BigDecimal("5000000");
    private static final BigDecimal USD_THRESHOLD = new BigDecimal("10000");

    private static final String SUSPICIOUS_NOTE = "High amount threshold exceeded";
    private static final String APPROVED_NOTE = "Automated check passed";

    // Approximate exchange rates to USD for threshold normalization
    private static final Map<String, BigDecimal> TO_USD_RATES = Map.of(
            "USD", BigDecimal.ONE,
            "KZT", new BigDecimal("0.002"), // 500 KZT = 1 USD
            "EUR", new BigDecimal("1.08"),
            "GBP", new BigDecimal("1.27"),
            "RUB", new BigDecimal("0.011")
    );

    public RiskAnalysisResult evaluateRisk(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            log.warn("Invalid evaluation inputs: amount={}, currency={}", amount, currency);
            return new RiskAnalysisResult(TransactionStatus.SUSPICIOUS, "Invalid transaction parameters");
        }

        String normalizedCurrency = currency.trim().toUpperCase();
        boolean isHighRisk;

        if ("KZT".equals(normalizedCurrency)) {
            isHighRisk = amount.compareTo(KZT_THRESHOLD) > 0;
            log.debug("Evaluating KZT transaction: amount={}, threshold={}, isExceeded={}",
                    amount, KZT_THRESHOLD, isHighRisk);
        } else if ("USD".equals(normalizedCurrency)) {
            isHighRisk = amount.compareTo(USD_THRESHOLD) > 0;
            log.debug("Evaluating USD transaction: amount={}, threshold={}, isExceeded={}",
                    amount, USD_THRESHOLD, isHighRisk);
        } else {
            BigDecimal rate = TO_USD_RATES.getOrDefault(normalizedCurrency, BigDecimal.ONE);
            BigDecimal usdEquivalent = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            isHighRisk = usdEquivalent.compareTo(USD_THRESHOLD) > 0;
            log.debug("Evaluating {} transaction: amount={}, usdEquivalent={}, threshold={}, isExceeded={}",
                    normalizedCurrency, amount, usdEquivalent, USD_THRESHOLD, isHighRisk);
        }

        if (isHighRisk) {
            log.warn("AML risk check verdict: SUSPICIOUS for amount={} {}. Reason: {}",
                    amount, normalizedCurrency, SUSPICIOUS_NOTE);
            return new RiskAnalysisResult(TransactionStatus.SUSPICIOUS, SUSPICIOUS_NOTE);
        }

        log.info("AML risk check verdict: APPROVED for amount={} {}. Verdict: {}",
                amount, normalizedCurrency, APPROVED_NOTE);
        return new RiskAnalysisResult(TransactionStatus.APPROVED, APPROVED_NOTE);
    }
}
