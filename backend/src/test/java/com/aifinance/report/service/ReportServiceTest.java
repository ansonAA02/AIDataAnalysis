package com.aifinance.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.service.AiAnalysisService;
import com.aifinance.finance.domain.MetricCode;
import com.aifinance.finance.domain.RiskLevel;
import com.aifinance.finance.domain.RiskType;
import com.aifinance.finance.dto.FinancialSnapshot;
import com.aifinance.finance.dto.RiskSignal;
import com.aifinance.finance.service.FinanceAnalysisService;
import com.aifinance.report.dto.ReportResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ReportServiceTest {

    @Test
    void monthlyReportBuildsFixedSectionsFromFinanceAndAiAnalysis() {
        FinanceAnalysisService financeAnalysisService = Mockito.mock(FinanceAnalysisService.class);
        AiAnalysisService aiAnalysisService = Mockito.mock(AiAnalysisService.class);
        when(financeAnalysisService.snapshot(4L)).thenReturn(snapshot());
        when(financeAnalysisService.budgetVariances(4L)).thenReturn(List.of(costVariance()));
        when(financeAnalysisService.risks(4L)).thenReturn(List.of(new RiskSignal(
                RiskType.BUDGET_OVERSPEND,
                RiskLevel.HIGH,
                "成本实际值超过预算，需要复盘预算执行。")));
        when(aiAnalysisService.summary(4L)).thenReturn(new AiAnswerResponse(
                "本期利润承压。",
                "成本高于预算。",
                "成本上涨压缩利润空间。",
                "预算超支风险较高。",
                "复盘供应商成本。"));
        when(aiAnalysisService.suggestions(4L)).thenReturn(List.of("复盘供应商成本", "提高回款优先级"));
        ReportService reportService = new ReportService(financeAnalysisService, aiAnalysisService);

        ReportResponse report = reportService.monthlyReport(4L);

        assertThat(report.periodId()).isEqualTo(4L);
        assertThat(report.periodLabel()).isEqualTo("2025-09");
        assertThat(report.title()).isEqualTo("2025-09 月度经营分析报告");
        assertThat(report.unit()).isEqualTo("万元");
        assertThat(report.sections()).extracting("code").containsExactly(
                "OVERVIEW",
                "REVENUE",
                "COST",
                "PROFIT",
                "CASH_FLOW",
                "BUDGET",
                "RISK",
                "SUGGESTION");
        assertThat(report.sections().getFirst().content()).contains("本期利润承压。");
        assertThat(section(report, "BUDGET")).contains("COST", "130.00", "16.67");
        assertThat(section(report, "RISK")).contains("BUDGET_OVERSPEND", "HIGH");
        assertThat(section(report, "SUGGESTION")).contains("复盘供应商成本", "提高回款优先级");
    }

    private String section(ReportResponse report, String code) {
        return report.sections()
                .stream()
                .filter(section -> section.code().equals(code))
                .findFirst()
                .orElseThrow()
                .content();
    }

    private FinancialSnapshot snapshot() {
        return new FinancialSnapshot(
                4L,
                "2025-09",
                new BigDecimal("1310.00"),
                new BigDecimal("910.00"),
                new BigDecimal("400.00"),
                new BigDecimal("245.00"),
                new BigDecimal("155.00"),
                new BigDecimal("120.00"),
                new BigDecimal("30.53"),
                new BigDecimal("11.83"),
                "万元");
    }

    private FinanceAnalysisService.BudgetVariance costVariance() {
        return new FinanceAnalysisService.BudgetVariance(
                MetricCode.COST,
                "成本",
                new BigDecimal("910.00"),
                new BigDecimal("780.00"),
                new BigDecimal("130.00"),
                new BigDecimal("16.67"),
                "万元");
    }
}
