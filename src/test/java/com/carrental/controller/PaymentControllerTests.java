package com.carrental.controller;

import com.carrental.dto.request.CreatePaymentRequest;
import com.carrental.dto.response.PaymentResponse;
import com.carrental.entity.enums.PaymentMethod;
import com.carrental.entity.enums.PaymentStatus;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private PaymentService paymentService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    private String validToken;
    private PaymentResponse testPaymentResponse;
    
    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        
        testPaymentResponse = PaymentResponse.builder()
            .id(1L)
            .rentalId(1L)
            .userId(1L)
            .amount(BigDecimal.valueOf(150))
            .refundedAmount(BigDecimal.ZERO)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .transactionId("TXN123456")
            .status(PaymentStatus.COMPLETED)
            .build();
    }
    
    @Test
    void testProcessPayment_Success() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .rentalId(1L)
            .amount(BigDecimal.valueOf(150))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .transactionId("TXN123456")
            .build();
        
        when(jwtTokenProvider.getUserIdFromToken(anyString())).thenReturn(1L);
        when(paymentService.processPayment(eq(1L), any(CreatePaymentRequest.class)))
            .thenReturn(testPaymentResponse);
        
        mockMvc.perform(post("/api/payments")
            .header("Authorization", validToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void testGetPayment_Success() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(testPaymentResponse);
        
        mockMvc.perform(get("/api/payments/1")
            .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }
}