package com.carrental.controller;

import com.carrental.dto.response.RevenueReport;
import com.carrental.dto.response.VehicleReport;
import com.carrental.dto.response.UserReport;
import com.carrental.service.AdminReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminReportControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AdminReportService adminReportService;
    
    private RevenueReport testRevenueReport;
    
    @BeforeEach
    void setUp() {
        testRevenueReport = RevenueReport.builder()
            .startDate(LocalDate.now().minusDays(30))
            .endDate(LocalDate.now())
            .period("Monthly")
            .totalRevenue(BigDecimal.valueOf(50000))
            .netRevenue(BigDecimal.valueOf(48000))
            .totalTransactions(100L)
            .successfulTransactions(95L)
            .failedTransactions(5L)
            .averageTransactionValue(BigDecimal.valueOf(500))
            .dailyBreakdown(new ArrayList<>())
            .build();
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGenerateRevenueReport_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        when(adminReportService.generateRevenueReport(startDate, endDate))
            .thenReturn(testRevenueReport);
        
        mockMvc.perform(get("/admin/reports/revenue")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.startDate").value(startDate.toString()))
            .andExpect(jsonPath("$.totalRevenue").value(50000));
    }
     
}