package com.aifinance.finance.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.aifinance.finance.domain.RiskLevel;
import com.aifinance.finance.domain.RiskType;
import com.aifinance.finance.dto.FinancialSnapshot;
import com.aifinance.finance.dto.RiskSignal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FinanceAnalysisServiceTest {

    @Autowired
    private FinanceAnalysisService financeAnalysisService;

    @Test
    void buildsFinancialSnapshotWithCalculatedMargins() {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(4L);

        assertThat(snapshot.periodLabel()).isEqualTo("2025-09");
        assertThat(snapshot.revenue()).isEqualByComparingTo(new BigDecimal("1310.00"));
        assertThat(snapshot.cost()).isEqualByComparingTo(new BigDecimal("910.00"));
        assertThat(snapshot.netProfit()).isEqualByComparingTo(new BigDecimal("155.00"));
        assertThat(snapshot.netMargin()).isEqualByComparingTo(new BigDecimal("11.83"));
        assertThat(snapshot.unit()).isEqualTo("万元");
    }

    @Test
    void returnsRecentTrendSnapshotsInPeriodOrder() {
        List<FinancialSnapshot> snapshots = financeAnalysisService.recentSnapshots(12);

        assertThat(snapshots).hasSize(12);
        assertThat(snapshots.getFirst().periodLabel()).isEqualTo("2025-06");
        assertThat(snapshots.getLast().periodLabel()).isEqualTo("2026-05");
    }

    @Test
    void identifiesCostBudgetAndProfitRisksForSeptember() {
        List<RiskSignal> risks = financeAnalysisService.risks(4L);

        assertThat(risks).extracting(RiskSignal::type)
                .contains(
                        RiskType.COST_GROWTH_FAST,
                        RiskType.BUDGET_OVERSPEND,
                        RiskType.PROFIT_MARGIN_DROP);
        assertThat(risks).extracting(RiskSignal::level)
                .contains(RiskLevel.HIGH);
    }

    @Test
    void identifiesCashFlowRiskForNegativeCashFlow() {
        List<RiskSignal> risks = financeAnalysisService.risks(5L);

        assertThat(risks).extracting(RiskSignal::type)
                .contains(RiskType.CASH_FLOW_RISK);
    }
}
