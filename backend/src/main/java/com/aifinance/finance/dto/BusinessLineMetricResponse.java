package com.aifinance.finance.dto;

import java.math.BigDecimal;

import com.aifinance.finance.domain.BusinessLineMetric;

public record BusinessLineMetricResponse(
        String businessLine,
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal grossProfit,
        BigDecimal netProfit,
        String unit) {

    public static BusinessLineMetricResponse from(BusinessLineMetric metric) {
        return new BusinessLineMetricResponse(
                metric.getBusinessLine(),
                metric.getRevenue(),
                metric.getCost(),
                metric.getGrossProfit(),
                metric.getNetProfit(),
                metric.getUnit());
    }
}
