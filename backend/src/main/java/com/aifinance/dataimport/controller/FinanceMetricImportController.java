package com.aifinance.dataimport.controller;

import com.aifinance.common.ApiResponse;
import com.aifinance.dataimport.dto.ImportCommitResponse;
import com.aifinance.dataimport.dto.ImportPreviewResponse;
import com.aifinance.dataimport.service.FinanceMetricImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// REST endpoints for the CSV-driven finance metric import wizard.
@RestController
@RequestMapping("/api/import/finance-metrics")
public class FinanceMetricImportController {

    private final FinanceMetricImportService service;

    public FinanceMetricImportController(FinanceMetricImportService service) {
        this.service = service;
    }

    // Parse the uploaded CSV and return validation results without persisting.
    @PostMapping("/preview")
    public ApiResponse<ImportPreviewResponse> preview(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(service.preview(file));
    }

    // Re-parse the CSV and upsert all valid rows into finance_metric.
    @PostMapping("/commit")
    public ApiResponse<ImportCommitResponse> commit(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(service.commit(file));
    }
}
