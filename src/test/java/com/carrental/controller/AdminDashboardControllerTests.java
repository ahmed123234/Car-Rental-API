package com.carrental.controller;

import com.carrental.dto.response.DashboardMetrics;
import com.carrental.service.AdminDashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AdminDashboardService adminDashboardService;
    
    private DashboardMetrics testMetrics;
    
    @BeforeEach
    void setUp() {
        testMetrics = DashboardMetrics.builder()
            .totalRentals(100L)
            .activeRentals(10L)
            .completedRentals(85L)
            .cancelledRentals(5L)
            .totalRevenue(BigDecimal.valueOf(50000))
            .dailyRevenue(BigDecimal.valueOf(1500))
            .monthlyRevenue(BigDecimal.valueOf(45000))
            .averageOrderValue(BigDecimal.valueOf(500))
            .totalVehicles(50L)
            .availableVehicles(35L)
            .maintenanceVehicles(5L)
            .fleetUtilizationRate(30.0)
            .totalUsers(200L)
            .activeUsers(150L)
            .newUsersThisMonth(20L)
            .totalPayments(100L)
            .completedPayments(95L)
            .pendingPayments(3L)
            .failedPayments(2L)
            .totalReviews(150L)
            .pendingReviews(10L)
            .averageRating(4.5)
            .totalRefunds(5L)
            .totalRefundAmount(BigDecimal.valueOf(2500))
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetDashboardMetrics_Success() throws Exception {
        when(adminDashboardService.getDashboardMetrics()).thenReturn(testMetrics);
        
        mockMvc.perform(get("/admin/dashboard/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRentals").value(100))
            .andExpect(jsonPath("$.totalRevenue").value(50000))
            .andExpect(jsonPath("$.totalVehicles").value(50))
            .andExpect(jsonPath("$.totalUsers").value(200));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRevenueTrend_Success() throws Exception {
        mockMvc.perform(get("/admin/dashboard/revenue-trend")
            .param("days", "30"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRevenueTrend_InvalidDays() throws Exception {
        mockMvc.perform(get("/admin/dashboard/revenue-trend")
            .param("days", "0"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetFleetUtilization_Success() throws Exception {
        when(adminDashboardService.getDashboardMetrics()).thenReturn(testMetrics);
        
        mockMvc.perform(get("/admin/dashboard/fleet-utilization"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalVehicles").value(50))
            .andExpect(jsonPath("$.availableVehicles").value(35))
            .andExpect(jsonPath("$.utilizationRate").value(30.0));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPaymentMetrics_Success() throws Exception {
        when(adminDashboardService.getDashboardMetrics()).thenReturn(testMetrics);
        
        mockMvc.perform(get("/admin/dashboard/payment-metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPayments").value(100))
            .andExpect(jsonPath("$.completedPayments").value(95));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserMetrics_Success() throws Exception {
        when(adminDashboardService.getDashboardMetrics()).thenReturn(testMetrics);
        
        mockMvc.perform(get("/admin/dashboard/user-metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUsers").value(200))
            .andExpect(jsonPath("$.activeUsers").value(150));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetReviewMetrics_Success() throws Exception {
        when(adminDashboardService.getDashboardMetrics()).thenReturn(testMetrics);
        
        mockMvc.perform(get("/admin/dashboard/review-metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalReviews").value(150))
            .andExpect(jsonPath("$.averageRating").value(4.5));
    }
    
    @Test
    void testGetDashboardMetrics_Unauthorized() throws Exception {
        mockMvc.perform(get("/admin/dashboard/metrics"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetDashboardMetrics_Forbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard/metrics"))
            .andExpect(status().isForbidden());
    }
}
