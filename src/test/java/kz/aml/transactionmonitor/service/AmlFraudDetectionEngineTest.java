package kz.aml.transactionmonitor.service;

import kz.aml.transactionmonitor.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmlFraudDetectionEngineTest {

    private AmlFraudDetectionEngine fraudDetectionEngine;

    @BeforeEach
    void setUp() {
        fraudDetectionEngine = new AmlFraudDetectionEngine();
    }

    @Test
    @DisplayName("KZT transaction under or equal to 5,000,000 should be APPROVED")
    void testKztUnderThresholdApproved() {
        RiskAnalysisResult result = fraudDetectionEngine.evaluateRisk(new BigDecimal("5000000"), "KZT");
        assertEquals(TransactionStatus.APPROVED, result.status());
        assertEquals("Automated check passed", result.riskNote());
    }

    @Test
    @DisplayName("KZT transaction exceeding 5,000,000 should be SUSPICIOUS")
    void testKztExceedingThresholdSuspicious() {
        RiskAnalysisResult result = fraudDetectionEngine.evaluateRisk(new BigDecimal("5000000.01"), "KZT");
        assertEquals(TransactionStatus.SUSPICIOUS, result.status());
        assertEquals("High amount threshold exceeded", result.riskNote());
    }

    @Test
    @DisplayName("USD transaction under or equal to 10,000 should be APPROVED")
    void testUsdUnderThresholdApproved() {
        RiskAnalysisResult result = fraudDetectionEngine.evaluateRisk(new BigDecimal("10000"), "USD");
        assertEquals(TransactionStatus.APPROVED, result.status());
        assertEquals("Automated check passed", result.riskNote());
    }

    @Test
    @DisplayName("USD transaction exceeding 10,000 should be SUSPICIOUS")
    void testUsdExceedingThresholdSuspicious() {
        RiskAnalysisResult result = fraudDetectionEngine.evaluateRisk(new BigDecimal("10000.01"), "USD");
        assertEquals(TransactionStatus.SUSPICIOUS, result.status());
        assertEquals("High amount threshold exceeded", result.riskNote());
    }

    @Test
    @DisplayName("EUR transaction exceeding equivalent of 10,000 USD should be SUSPICIOUS")
    void testEurEquivalentSuspicious() {
        // 10,000 EUR * 1.08 = 10,800 USD > 10,000 USD
        RiskAnalysisResult result = fraudDetectionEngine.evaluateRisk(new BigDecimal("10000"), "EUR");
        assertEquals(TransactionStatus.SUSPICIOUS, result.status());
        assertEquals("High amount threshold exceeded", result.riskNote());
    }
}
