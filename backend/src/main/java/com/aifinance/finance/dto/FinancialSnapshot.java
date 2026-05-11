package com.aifinance.finance.dto;

import java.math.BigDecimal;

public record FinancialSnapshot(
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
}
