package com.aifinance.report.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.service.AiAnalysisService;
import com.aifinance.finance.dto.FinancialSnapshot;
import com.aifinance.finance.dto.RiskSignal;
import com.aifinance.finance.service.FinanceAnalysisService;
import com.aifinance.report.dto.ReportResponse;
import com.aifinance.report.dto.ReportSectionResponse;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final FinanceAnalysisService financeAnalysisService;
    private final AiAnalysisService aiAnalysisService;

    public ReportService(FinanceAnalysisService financeAnalysisService, AiAnalysisService aiAnalysisService) {
        this.financeAnalysisService = financeAnalysisService;
        this.aiAnalysisService = aiAnalysisService;
    }

    public ReportResponse monthlyReport(Long periodId) {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(periodId);
        List<FinanceAnalysisService.BudgetVariance> budgetVariances = financeAnalysisService.budgetVariances(periodId);
        List<RiskSignal> risks = financeAnalysisService.risks(periodId);

        // Fetch AI responses concurrently to reduce total response time
        CompletableFuture<AiAnswerResponse> summaryFuture = CompletableFuture.supplyAsync(() -> aiAnalysisService.summary(periodId));
        CompletableFuture<List<String>> suggestionsFuture = CompletableFuture.supplyAsync(() -> aiAnalysisService.suggestions(periodId));
        
        CompletableFuture.allOf(summaryFuture, suggestionsFuture).join();
        
        AiAnswerResponse summary = summaryFuture.join();
        List<String> suggestions = suggestionsFuture.join();

        return new ReportResponse(
                snapshot.periodId(),
                snapshot.periodLabel(),
                snapshot.periodLabel() + " 月度经营分析报告",
                snapshot.unit(),
                List.of(
                        section("OVERVIEW", "经营概览", summary.conclusion() + " " + summary.analysis()),
                        section("REVENUE", "收入", "收入 " + snapshot.revenue() + " " + snapshot.unit() + "。"),
                        section("COST", "成本", "成本 " + snapshot.cost() + " " + snapshot.unit() + "。"),
                        section("PROFIT", "利润", "毛利 " + snapshot.grossProfit() + " " + snapshot.unit()
                                + "，净利 " + snapshot.netProfit() + " " + snapshot.unit()
                                + "，净利率 " + snapshot.netMargin() + "%。"),
                        section("CASH_FLOW", "现金流", "经营现金流 " + snapshot.operatingCashFlow() + " " + snapshot.unit() + "。"),
                        section("BUDGET", "预算", budgetContent(budgetVariances)),
                        section("RISK", "风险", riskContent(risks)),
                        section("SUGGESTION", "建议", String.join("\n", suggestions))));
    }

    private ReportSectionResponse section(String code, String title, String content) {
        return new ReportSectionResponse(code, title, content);
    }

    private String budgetContent(List<FinanceAnalysisService.BudgetVariance> budgetVariances) {
        return budgetVariances.stream()
                .map(variance -> variance.metricCode()
                        + " 实际 " + variance.actualValue()
                        + "，预算 " + variance.budgetValue()
                        + "，偏差 " + variance.varianceAmount()
                        + "，偏差率 " + variance.varianceRate() + "%。")
                .collect(Collectors.joining("\n"));
    }

    private String riskContent(List<RiskSignal> risks) {
        return risks.stream()
                .map(risk -> risk.type() + " " + risk.level() + "：" + risk.message())
                .collect(Collectors.joining("\n"));
    }
}
