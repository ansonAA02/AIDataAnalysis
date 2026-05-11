package com.aifinance.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void overviewEndpointReturnsKpiSnapshotForPeriod() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.periodLabel").value("2025-09"))
                .andExpect(jsonPath("$.data.revenue").value(1310.00))
                .andExpect(jsonPath("$.data.cost").value(910.00))
                .andExpect(jsonPath("$.data.netProfit").value(155.00))
                .andExpect(jsonPath("$.data.netMargin").value(11.83))
                .andExpect(jsonPath("$.data.unit").value("万元"));
    }

    @Test
    void trendsEndpointReturnsRecentMonthlyTrendPoints() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends").param("months", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(12))
                .andExpect(jsonPath("$.data[0].periodLabel").value("2025-06"))
                .andExpect(jsonPath("$.data[11].periodLabel").value("2026-05"));
    }

    @Test
    void risksEndpointReturnsRiskAlertsForPeriod() throws Exception {
        mockMvc.perform(get("/api/dashboard/risks").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[?(@.type == 'COST_GROWTH_FAST')].level").value("HIGH"))
                .andExpect(jsonPath("$.data[?(@.type == 'BUDGET_OVERSPEND')].level").value("HIGH"))
                .andExpect(jsonPath("$.data[?(@.type == 'PROFIT_MARGIN_DROP')].level").value("HIGH"));
    }
}
