package kz.aml.transactionmonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.aml.transactionmonitor.dto.TransactionRequestDto;
import kz.aml.transactionmonitor.dto.TransactionResponseDto;
import kz.aml.transactionmonitor.exception.GlobalExceptionHandler;
import kz.aml.transactionmonitor.exception.TransactionNotFoundException;
import kz.aml.transactionmonitor.model.TransactionStatus;
import kz.aml.transactionmonitor.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/transactions with valid payload should return 201 Created")
    void testCreateTransactionSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        TransactionRequestDto request = TransactionRequestDto.builder()
                .accountFrom("KZ111")
                .accountTo("KZ222")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .build();

        TransactionResponseDto response = TransactionResponseDto.builder()
                .id(id)
                .accountFrom("KZ111")
                .accountTo("KZ222")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .riskNote("Pending AML automated check")
                .createdAt(Instant.now())
                .build();

        when(transactionService.createTransaction(any(TransactionRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.accountFrom").value("KZ111"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions with invalid payload should return 400 Bad Request with field errors")
    void testCreateTransactionValidationFailure() throws Exception {
        TransactionRequestDto invalidRequest = TransactionRequestDto.builder()
                .accountFrom("")
                .accountTo("")
                .amount(new BigDecimal("0.00"))
                .currency("US")
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.accountFrom").exists())
                .andExpect(jsonPath("$.fieldErrors.accountTo").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.currency").exists());
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} found should return 200 OK")
    void testGetTransactionFound() throws Exception {
        UUID id = UUID.randomUUID();
        TransactionResponseDto response = TransactionResponseDto.builder()
                .id(id)
                .accountFrom("KZ111")
                .accountTo("KZ222")
                .amount(new BigDecimal("5000.00"))
                .currency("USD")
                .status(TransactionStatus.APPROVED)
                .riskNote("Automated check passed")
                .createdAt(Instant.now())
                .build();

        when(transactionService.getTransactionById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} not found should return 404 Not Found")
    void testGetTransactionNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(transactionService.getTransactionById(id)).thenThrow(new TransactionNotFoundException(id));

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format("Transaction with ID '%s' not found", id)));
    }
}
