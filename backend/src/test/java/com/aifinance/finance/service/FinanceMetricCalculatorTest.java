package com.aifinance.finance.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.aifinance.finance.domain.RiskLevel;
import com.aifinance.finance.domain.RiskType;
import com.aifinance.finance.dto.VarianceResult;
import org.junit.jupiter.api.Test;

class FinanceMetricCalculatorTest {

    private final FinanceMetricCalculator calculator = new FinanceMetricCalculator();

    @Test
    void calculatesPercentagesWithTwoDecimalPlacesUsingBigDecimal() {
        BigDecimal margin = calculator.percentage(new BigDecimal("155.00"), new BigDecimal("1310.00"));

        assertThat(margin).isEqualByComparingTo(new BigDecimal("11.83"));
        assertThat(margin.scale()).isEqualTo(2);
    }

    @Test
    void returnsZeroPercentWhenDenominatorIsZero() {
        BigDecimal margin = calculator.percentage(new BigDecimal("100.00"), BigDecimal.ZERO);

        assertThat(margin).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(margin.scale()).isEqualTo(2);
    }

    @Test
    void calculatesBudgetVarianceAmountAndRate() {
        VarianceResult variance = calculator.variance(
                new BigDecimal("910.00"),
                new BigDecimal("780.00"));

        assertThat(variance.actualValue()).isEqualByComparingTo(new BigDecimal("910.00"));
        assertThat(variance.budgetValue()).isEqualByComparingTo(new BigDecimal("780.00"));
        assertThat(variance.varianceAmount()).isEqualByComparingTo(new BigDecimal("130.00"));
        assertThat(variance.varianceRate()).isEqualByComparingTo(new BigDecimal("16.67"));
        assertThat(variance.varianceRate().scale()).isEqualTo(2);
    }

    @Test
    void exposesRequiredRiskLevelsAndTypes() {
        assertThat(RiskLevel.values()).containsExactly(RiskLevel.LOW, RiskLevel.MEDIUM, RiskLevel.HIGH);
        assertThat(RiskType.values()).containsExactly(
                RiskType.CASH_FLOW_RISK,
                RiskType.PROFIT_MARGIN_DROP,
                RiskType.COST_GROWTH_FAST,
                RiskType.BUDGET_OVERSPEND,
                RiskType.REVENUE_DECLINE);
    }
}
