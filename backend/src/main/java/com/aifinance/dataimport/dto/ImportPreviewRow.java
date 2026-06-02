package com.aifinance.dataimport.dto;

import java.math.BigDecimal;

// One parsed row from a CSV preview. `error` is non-null when validation failed.
public record ImportPreviewRow(
        int rowNumber,
        Long periodId,
        String periodLabel,
        String metricCode,
        String metricName,
        String metricCategory,
        BigDecimal actualValue,
        String unit,
        boolean valid,
        String error) {
}
