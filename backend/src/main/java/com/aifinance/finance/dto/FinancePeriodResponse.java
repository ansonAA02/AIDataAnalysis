package com.aifinance.finance.dto;

import com.aifinance.finance.domain.FinancePeriod;

public record FinancePeriodResponse(
        Long id,
        Integer year,
        Integer month,
        Integer quarter,
        String periodLabel) {

    public static FinancePeriodResponse from(FinancePeriod period) {
        return new FinancePeriodResponse(
                period.getId(),
                period.getYearValue(),
                period.getMonthValue(),
                period.getQuarterValue(),
                period.getPeriodLabel());
    }
}
