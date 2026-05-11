package com.aifinance.finance.dto;

import java.math.BigDecimal;

import com.aifinance.finance.domain.FinanceMetric;

public record FinanceMetricResponse(
        Long id,
        Long periodId,
        String metricCode,
        String metricName,
        String metricCategory,
        BigDecimal actualValue,
        String unit) {

    public static FinanceMetricResponse from(FinanceMetric metric) {
        return new FinanceMetricResponse(
                metric.getId(),
                metric.getPeriodId(),
                metric.getMetricCode().name(),
                metric.getMetricName(),
                metric.getMetricCategory(),
                metric.getActualValue(),
                metric.getUnit());
    }
}
