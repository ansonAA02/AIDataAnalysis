package com.aifinance.report.dto;

import java.util.List;

public record ReportResponse(
        Long periodId,
        String periodLabel,
        String title,
        String unit,
        List<ReportSectionResponse> sections) {
}
