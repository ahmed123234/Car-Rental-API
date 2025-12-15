package com.carrental.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class PaymentUtil {
    
    private static final String INVOICE_PREFIX = "INV";
    
    /**
     * Generate unique invoice number
     */
    public static String generateInvoiceNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return INVOICE_PREFIX + "-" + timestamp + "-" + randomPart;
    }
    
    /**
     * Calculate cancellation refund based on policy
     */
    public static BigDecimal calculateCancellationRefund(BigDecimal totalCost, LocalDateTime pickupDate) {
        long hoursUntilPickup = ChronoUnit.HOURS.between(LocalDateTime.now(), pickupDate);
        
        if (hoursUntilPickup > 168) { // More than 7 days
            return totalCost; // Full refund
        } else if (hoursUntilPickup > 72) { // 3-7 days
            return totalCost.multiply(BigDecimal.valueOf(0.70)); // 70% refund
        } else if (hoursUntilPickup > 24) { // 24-72 hours
            return totalCost.multiply(BigDecimal.valueOf(0.50)); // 50% refund
        } else { // Less than 24 hours
            return BigDecimal.ZERO; // No refund
        }
    }
    
    /**
     * Calculate late return fees
     */
    public static BigDecimal calculateLateFees(LocalDateTime returnDate, BigDecimal dailyRate) {
        long hoursLate = ChronoUnit.HOURS.between(returnDate, LocalDateTime.now());
        
        if (hoursLate <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal hourlyRate = dailyRate.divide(BigDecimal.valueOf(24), 2, java.math.RoundingMode.HALF_UP);
        return hourlyRate.multiply(BigDecimal.valueOf(hoursLate));
    }
    
    /**
     * Validate payment amount
     */
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }
    
    /**
     * Format currency for display
     */
    public static String formatCurrency(BigDecimal amount) {
        return String.format("$%.2f", amount);
    }
}
