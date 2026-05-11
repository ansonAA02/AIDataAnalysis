package com.aifinance.report.controller;

import com.aifinance.common.ApiResponse;
import com.aifinance.report.dto.ReportResponse;
import com.aifinance.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ApiResponse<ReportResponse> monthly(@RequestParam Long periodId) {
        return ApiResponse.success(reportService.monthlyReport(periodId));
    }
}
