package com.aifinance.dashboard.dto;

import java.math.BigDecimal;

import com.aifinance.finance.dto.FinancialSnapshot;

public record DashboardOverviewResponse(
        Long periodId,
        String periodLabel,
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal grossProfit,
        BigDecimal operatingExpense,
        BigDecimal netProfit,
        BigDecimal operatingCashFlow,
        BigDecimal grossMargin,
        BigDecimal netMargin,
        String unit) {

    public static DashboardOverviewResponse from(FinancialSnapshot snapshot) {
        return new DashboardOverviewResponse(
                snapshot.periodId(),
                snapshot.periodLabel(),
                snapshot.revenue(),
                snapshot.cost(),
                snapshot.grossProfit(),
                snapshot.operatingExpense(),
                snapshot.netProfit(),
                snapshot.operatingCashFlow(),
                snapshot.grossMargin(),
                snapshot.netMargin(),
                snapshot.unit());
    }
}
