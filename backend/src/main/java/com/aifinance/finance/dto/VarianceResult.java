package com.aifinance.finance.dto;

import java.math.BigDecimal;

public record VarianceResult(
        BigDecimal actualValue,
        BigDecimal budgetValue,
        BigDecimal varianceAmount,
        BigDecimal varianceRate) {
}
