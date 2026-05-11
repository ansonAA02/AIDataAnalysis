package com.aifinance.finance.controller;

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
class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void periodsEndpointReturnsTwelveSeededPeriods() throws Exception {
        mockMvc.perform(get("/api/finance/periods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(12))
                .andExpect(jsonPath("$.data[0].periodLabel").value("2025-06"))
                .andExpect(jsonPath("$.data[11].periodLabel").value("2026-05"));
    }

    @Test
    void metricsEndpointReturnsMetricsForRequestedPeriod() throws Exception {
        mockMvc.perform(get("/api/finance/metrics").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].actualValue").value(910.00))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].unit").value("万元"));
    }

    @Test
    void businessLinesEndpointReturnsSeededLinesForPeriod() throws Exception {
        mockMvc.perform(get("/api/finance/business-lines").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].businessLine").value("企业服务"))
                .andExpect(jsonPath("$.data[1].businessLine").value("订阅业务"));
    }
}
