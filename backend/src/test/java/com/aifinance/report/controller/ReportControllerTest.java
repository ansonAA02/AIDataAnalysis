package com.aifinance.report.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.aifinance.report.dto.ReportResponse;
import com.aifinance.report.dto.ReportSectionResponse;
import com.aifinance.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void monthlyEndpointReturnsStructuredMonthlyReport() throws Exception {
        when(reportService.monthlyReport(4L)).thenReturn(new ReportResponse(
                4L,
                "2025-09",
                "2025-09 月度经营分析报告",
                "万元",
                List.of(
                        new ReportSectionResponse("OVERVIEW", "经营概览", "本期利润承压。"),
                        new ReportSectionResponse("REVENUE", "收入", "收入 1310.00 万元。"),
                        new ReportSectionResponse("COST", "成本", "成本 910.00 万元。"),
                        new ReportSectionResponse("PROFIT", "利润", "净利 155.00 万元。"),
                        new ReportSectionResponse("CASH_FLOW", "现金流", "经营现金流 120.00 万元。"),
                        new ReportSectionResponse("BUDGET", "预算", "COST 偏差率 16.67%。"),
                        new ReportSectionResponse("RISK", "风险", "BUDGET_OVERSPEND HIGH。"),
                        new ReportSectionResponse("SUGGESTION", "建议", "复盘供应商成本。"))));

        mockMvc.perform(get("/api/reports/monthly").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.periodId").value(4))
                .andExpect(jsonPath("$.data.periodLabel").value("2025-09"))
                .andExpect(jsonPath("$.data.title").value("2025-09 月度经营分析报告"))
                .andExpect(jsonPath("$.data.sections.length()").value(8))
                .andExpect(jsonPath("$.data.sections[0].code").value("OVERVIEW"))
                .andExpect(jsonPath("$.data.sections[7].code").value("SUGGESTION"));
    }
}
