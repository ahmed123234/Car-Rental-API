package com.carrental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReport {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String period; // Daily, Weekly, Monthly, Annual
    
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    
    private BigDecimal averageTransactionValue;
    private BigDecimal highestDailyRevenue;
    private BigDecimal lowestDailyRevenue;
    
    private List<DailyRevenue> dailyBreakdown;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Long transactionCount;
    }
}
