package com.aifinance.budget.dto;

import java.math.BigDecimal;

import com.aifinance.finance.domain.MetricCode;
import com.aifinance.finance.service.FinanceAnalysisService;

public record BudgetVarianceResponse(
        MetricCode metricCode,
        String metricName,
        BigDecimal actualValue,
        BigDecimal budgetValue,
        BigDecimal varianceAmount,
        BigDecimal varianceRate,
        String unit) {

    public static BudgetVarianceResponse from(FinanceAnalysisService.BudgetVariance variance) {
        return new BudgetVarianceResponse(
                variance.metricCode(),
                variance.metricName(),
                variance.actualValue(),
                variance.budgetValue(),
                variance.varianceAmount(),
                variance.varianceRate(),
                variance.unit());
    }
}
