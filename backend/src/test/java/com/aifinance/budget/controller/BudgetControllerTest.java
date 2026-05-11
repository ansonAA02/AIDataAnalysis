package com.aifinance.budget.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.service.AiAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    @Test
    void variancesEndpointReturnsCoreMetricBudgetVariance() throws Exception {
        mockMvc.perform(get("/api/budget/variances").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].actualValue").value(910.00))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].budgetValue").value(780.00))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].varianceAmount").value(130.00))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].varianceRate").value(16.67))
                .andExpect(jsonPath("$.data[?(@.metricCode == 'COST')].unit").value("万元"));
    }

    @Test
    void explanationEndpointReturnsStructuredAiBudgetExplanation() throws Exception {
        when(aiAnalysisService.budgetExplanation(4L)).thenReturn(new AiAnswerResponse(
                "成本预算超支是本期主要偏差。",
                "成本实际 910 万元，预算 780 万元。",
                "供应商和交付成本上涨。",
                "利润率和现金流承压。",
                "复盘供应商成本并收紧费用审批。"));

        mockMvc.perform(get("/api/budget/explanation").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.periodId").value(4))
                .andExpect(jsonPath("$.data.explanation.conclusion").value("成本预算超支是本期主要偏差。"))
                .andExpect(jsonPath("$.data.explanation.evidence").exists())
                .andExpect(jsonPath("$.data.explanation.analysis").exists())
                .andExpect(jsonPath("$.data.explanation.risk").exists())
                .andExpect(jsonPath("$.data.explanation.recommendation").exists());
    }
}
