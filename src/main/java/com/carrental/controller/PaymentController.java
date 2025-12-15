package com.carrental.controller;

import com.carrental.dto.request.CreatePaymentRequest;
import com.carrental.dto.request.ProcessRefundRequest;
import com.carrental.dto.response.InvoiceResponse;
import com.carrental.dto.response.PaymentResponse;
import com.carrental.dto.response.RefundResponse;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments, invoices, and refunds")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Process a payment for a rental")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "404", description = "Rental not found"),
        @ApiResponse(responseCode = "409", description = "Payment already exists")
    })
    public ResponseEntity<PaymentResponse> processPayment(
        @Valid @RequestBody CreatePaymentRequest request,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Processing payment for rental: {}", request.getRentalId());
        
        PaymentResponse response = paymentService.processPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get payment details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPayment(
        @Parameter(description = "Payment ID") @PathVariable Long id) {
        
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rental/{rentalId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get all payments for a rental")
    public ResponseEntity<List<PaymentResponse>> getRentalPayments(
        @Parameter(description = "Rental ID") @PathVariable Long rentalId) {
        
        List<PaymentResponse> response = paymentService.getRentalPayments(rentalId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get payment history for current user")
    public ResponseEntity<Page<PaymentResponse>> getUserPaymentHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<PaymentResponse> response = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Request refund for a payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<RefundResponse> refundPayment(
        @Parameter(description = "Payment ID") @PathVariable Long id,
        @Valid @RequestBody ProcessRefundRequest request) {
        
        log.info("Processing refund for payment: {}", id);
        RefundResponse response = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rental/{rentalId}/invoice")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get invoice for a rental")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoice found or generated"),
        @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public ResponseEntity<InvoiceResponse> generateInvoice(
        @Parameter(description = "Rental ID") @PathVariable Long rentalId) {
        
        log.info("Generating invoice for rental: {}", rentalId);
        InvoiceResponse response = paymentService.generateInvoice(rentalId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rental/{rentalId}/total-cost")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Calculate total cost for a rental (including taxes and fees)")
    public ResponseEntity<BigDecimal> calculateTotalCost(
        @Parameter(description = "Rental ID") @PathVariable Long rentalId) {
        
        BigDecimal totalCost = paymentService.calculateTotalCost(rentalId);
        return ResponseEntity.ok(totalCost);
    }
    
    @GetMapping("/admin/daily-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get daily revenue (Admin only)")
    public ResponseEntity<BigDecimal> getDailyRevenue() {
        BigDecimal revenue = paymentService.getDailyRevenue();
        return ResponseEntity.ok(revenue);
    }
    
    @GetMapping("/admin/monthly-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get monthly revenue (Admin only)")
    public ResponseEntity<BigDecimal> getMonthlyRevenue() {
        BigDecimal revenue = paymentService.getMonthlyRevenue();
        return ResponseEntity.ok(revenue);
    }
}
