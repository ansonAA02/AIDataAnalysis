package com.aifinance.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.aifinance.finance.dto.VarianceResult;
import org.springframework.stereotype.Component;

@Component
public class FinanceMetricCalculator {

    private static final int MONEY_SCALE = 2;

    public BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || BigDecimal.ZERO.compareTo(denominator) == 0) {
            return scale(BigDecimal.ZERO);
        }

        return numerator
                .multiply(new BigDecimal("100"))
                .divide(denominator, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public VarianceResult variance(BigDecimal actualValue, BigDecimal budgetValue) {
        BigDecimal varianceAmount = scale(actualValue.subtract(budgetValue));
        BigDecimal varianceRate = percentage(varianceAmount, budgetValue);

        return new VarianceResult(
                scale(actualValue),
                scale(budgetValue),
                varianceAmount,
                varianceRate);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
