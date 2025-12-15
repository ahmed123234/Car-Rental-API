package com.carrental.service;

import com.carrental.dto.request.CreatePaymentRequest;
import com.carrental.dto.request.ProcessRefundRequest;
import com.carrental.dto.response.InvoiceResponse;
import com.carrental.dto.response.PaymentResponse;
import com.carrental.dto.response.RefundResponse;
import com.carrental.entity.*;
import com.carrental.entity.enums.*;
import com.carrental.exception.PaymentException;
import com.carrental.mapper.InvoiceMapper;
import com.carrental.mapper.PaymentMapper;
import com.carrental.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private InvoiceRepository invoiceRepository;
    
    @Mock
    private RefundRepository refundRepository;
    
    @Mock
    private RentalRepository rentalRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PaymentMapper paymentMapper;
    
    @Mock
    private InvoiceMapper invoiceMapper;
    
    @InjectMocks
    private PaymentService paymentService;
    
    private User testUser;
    private Vehicle testVehicle;
    private Rental testRental;
    private Payment testPayment;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("user@test.com")
            .firstName("John")
            .lastName("Doe")
            .build();
        
        testVehicle = Vehicle.builder()
            .id(1L)
            .make("Toyota")
            .model("Camry")
            .dailyRate(BigDecimal.valueOf(50))
            .build();
        
        LocalDateTime now = LocalDateTime.now();
        testRental = Rental.builder()
            .id(1L)
            .user(testUser)
            .vehicle(testVehicle)
            .pickupDate(now.plusDays(2))
            .returnDate(now.plusDays(5))
            .totalCost(BigDecimal.valueOf(150))
            .status(RentalStatus.PENDING)
            .build();
        
        testPayment = Payment.builder()
            .id(1L)
            .rental(testRental)
            .user(testUser)
            .amount(BigDecimal.valueOf(150))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .transactionId("TXN123456")
            .status(PaymentStatus.COMPLETED)
            .build();
    }
    
    @Test
    void testProcessPayment_Success() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .rentalId(1L)
            .amount(BigDecimal.valueOf(150))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .transactionId("TXN123456")
            .build();
        
        when(paymentRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(paymentRepository.findByRentalIdAndStatus(anyLong(), any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(testPayment)).thenReturn(PaymentResponse.builder()
            .id(1L)
            .rentalId(1L)
            .status(PaymentStatus.COMPLETED)
            .build());
        
        PaymentResponse response = paymentService.processPayment(1L, request);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
    
    @Test
    void testProcessPayment_InvalidAmount() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .rentalId(1L)
            .amount(BigDecimal.ZERO)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .transactionId("TXN123456")
            .build();
        
        assertThrows(PaymentException.class, () -> paymentService.processPayment(1L, request));
    }
    
    @Test
    void testProcessPayment_DuplicateTransaction() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
            .rentalId(1L)
            .amount(BigDecimal.valueOf(150))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .transactionId("TXN123456")
            .build();
        
        when(paymentRepository.findByTransactionId("TXN123456")).thenReturn(Optional.of(testPayment));
        
        assertThrows(PaymentException.class, () -> paymentService.processPayment(1L, request));
    }
    
    @Test
    void testRefundPayment_Success() {
        ProcessRefundRequest request = ProcessRefundRequest.builder()
            .amount(BigDecimal.valueOf(75))
            .reason("Customer requested cancellation")
            .build();
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(refundRepository.save(any(Refund.class))).thenReturn(Refund.builder()
            .id(1L)
            .payment(testPayment)
            .rental(testRental)
            .amount(BigDecimal.valueOf(75))
            .status(RefundStatus.INITIATED)
            .build());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        
        RefundResponse response = paymentService.refundPayment(1L, request);
        
        assertNotNull(response);
        assertEquals(RefundStatus.INITIATED, response.getStatus());
        verify(refundRepository, times(1)).save(any(Refund.class));
    }
    
    @Test
    void testGenerateInvoice_Success() {
        when(invoiceRepository.findByRentalId(1L)).thenReturn(Optional.empty());
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(Invoice.builder()
            .id(1L)
            .rental(testRental)
            .invoiceNumber("INV-123456")
            .totalAmount(BigDecimal.valueOf(165))
            .build());
        when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(InvoiceResponse.builder()
            .id(1L)
            .invoiceNumber("INV-123456")
            .build());
        
        InvoiceResponse response = paymentService.generateInvoice(1L);
        
        assertNotNull(response);
        assertEquals("INV-123456", response.getInvoiceNumber());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }
}

