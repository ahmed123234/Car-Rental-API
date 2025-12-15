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
import com.carrental.util.PaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RefundRepository refundRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final InvoiceMapper invoiceMapper;
    
    // Tax rate (can be made configurable)
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.10);
    
    /**
     * Process a payment for a rental
     */
    public PaymentResponse processPayment(Long userId, CreatePaymentRequest request) {
        log.info("Processing payment for rental: {}, user: {}", request.getRentalId(), userId);
        
        // Validate payment amount
        if (!PaymentUtil.isValidAmount(request.getAmount())) {
            throw new PaymentException("Invalid payment amount");
        }
        
        // Check if transaction ID already exists
        if (paymentRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new PaymentException("Payment with this transaction ID already exists");
        }
        
        // Fetch rental and validate
        Rental rental = rentalRepository.findById(request.getRentalId())
            .orElseThrow(() -> new PaymentException("Rental not found"));
        
        if (!rental.getUser().getId().equals(userId)) {
            throw new PaymentException("Unauthorized: Payment does not belong to user");
        }
        
        // Validate payment amount matches rental cost
        if (request.getAmount().compareTo(rental.getTotalCost()) != 0) {
            throw new PaymentException("Payment amount must match rental total cost");
        }
        
        // Check if payment already exists for this rental
        if (paymentRepository.findByRentalIdAndStatus(request.getRentalId(), PaymentStatus.COMPLETED).isPresent()) {
            throw new PaymentException("Payment already completed for this rental");
        }
        
        // Create payment
        Payment payment = Payment.builder()
            .rental(rental)
            .user(rental.getUser())
            .amount(request.getAmount())
            .refundedAmount(BigDecimal.ZERO)
            .paymentMethod(request.getPaymentMethod())
            .transactionId(request.getTransactionId())
            .status(PaymentStatus.COMPLETED)
            .description(request.getDescription())
            .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully with ID: {}", savedPayment.getId());
        
        return paymentMapper.toResponse(savedPayment);
    }
    
    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        log.debug("Fetching payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        
        return paymentMapper.toResponse(payment);
    }
    
    /**
     * Get all payments for a rental
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getRentalPayments(Long rentalId) {
        log.debug("Fetching payments for rental: {}", rentalId);
        
        return paymentRepository.findByRentalId(rentalId).stream()
            .map(paymentMapper::toResponse)
            .toList();
    }
    
    /**
     * Get all payments for a user
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        log.debug("Fetching payments for user: {}", userId);
        
        return paymentRepository.findByUserId(userId, pageable)
            .map(paymentMapper::toResponse);
    }
    
    /**
     * Process refund for a payment
     */
    public RefundResponse refundPayment(Long paymentId, ProcessRefundRequest request) {
        log.info("Processing refund for payment: {}", paymentId);
        
        // Fetch payment
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Only completed payments can be refunded");
        }
        
        // Validate refund amount
        BigDecimal refundableAmount = payment.getAmount()
            .subtract(payment.getRefundedAmount() == null ? BigDecimal.ZERO : payment.getRefundedAmount());
        
        if (request.getAmount().compareTo(refundableAmount) > 0) {
            throw new PaymentException("Refund amount exceeds refundable balance");
        }
        
        // Create refund
        Refund refund = Refund.builder()
            .payment(payment)
            .rental(payment.getRental())
            .amount(request.getAmount())
            .reason(request.getReason())
            .status(RefundStatus.INITIATED)
            .build();
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Update payment refunded amount
        BigDecimal newRefundedAmount = (payment.getRefundedAmount() == null ? BigDecimal.ZERO : payment.getRefundedAmount())
            .add(request.getAmount());
        
        if (newRefundedAmount.compareTo(payment.getAmount()) >= 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        
        payment.setRefundedAmount(newRefundedAmount);
        paymentRepository.save(payment);
        
        log.info("Refund created successfully with ID: {}", savedRefund.getId());
        
        return RefundResponse.builder()
            .id(savedRefund.getId())
            .paymentId(savedRefund.getPayment().getId())
            .rentalId(savedRefund.getRental().getId())
            .amount(savedRefund.getAmount())
            .status(savedRefund.getStatus())
            .reason(savedRefund.getReason())
            .createdAt(savedRefund.getCreatedAt())
            .build();
    }
    
    /**
     * Generate invoice for a rental
     */
    public InvoiceResponse generateInvoice(Long rentalId) {
        log.info("Generating invoice for rental: {}", rentalId);
        
        // Check if invoice already exists
        if (invoiceRepository.findByRentalId(rentalId).isPresent()) {
            log.warn("Invoice already exists for rental: {}", rentalId);
            Invoice existingInvoice = invoiceRepository.findByRentalId(rentalId).get();
            return invoiceMapper.toResponse(existingInvoice);
        }
        
        // Fetch rental
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new PaymentException("Rental not found"));
        
        // Calculate amounts
        BigDecimal subtotal = rental.getTotalCost();
        BigDecimal taxes = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO; // Can be enhanced with discount codes
        BigDecimal totalAmount = subtotal.add(taxes).subtract(discount);
        
        // Create invoice
        Invoice invoice = Invoice.builder()
            .rental(rental)
            .invoiceNumber(PaymentUtil.generateInvoiceNumber())
            .subtotal(subtotal)
            .taxes(taxes)
            .discount(discount)
            .totalAmount(totalAmount)
            .notes("Invoice for rental " + rentalId)
            .build();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice generated successfully with ID: {}", savedInvoice.getId());
        
        return invoiceMapper.toResponse(savedInvoice);
    }
    
    /**
     * Calculate total cost for a rental (including taxes and fees)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCost(Long rentalId) {
        log.debug("Calculating total cost for rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new PaymentException("Rental not found"));
        
        BigDecimal baseCost = rental.getTotalCost();
        BigDecimal additionalFees = rental.getAdditionalFees() != null ? rental.getAdditionalFees() : BigDecimal.ZERO;
        BigDecimal taxes = baseCost.add(additionalFees).multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        
        return baseCost.add(additionalFees).add(taxes);
    }
    
    /**
     * Get daily revenue
     */
    @Transactional(readOnly = true)
    public BigDecimal getDailyRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return paymentRepository.calculateTotalRevenueFrom(startOfDay);
    }
    
    /**
     * Get monthly revenue
     */
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return paymentRepository.calculateTotalRevenueFrom(startOfMonth);
    }
    
    /**
     * Process pending refunds (scheduled task)
     */
    public void processPendingRefunds() {
        log.info("Processing pending refunds");
        
        List<Refund> refunds = refundRepository.findStuckProcessingRefunds(
            LocalDateTime.now().minusHours(24)
        );
        
        for (Refund refund : refunds) {
            log.warn("Refund stuck in processing: {}", refund.getId());
            // Could send notification or escalate
        }
    }
}