package com.aifinance.finance.controller;

import java.util.List;

import com.aifinance.common.ApiResponse;
import com.aifinance.finance.dto.BusinessLineMetricResponse;
import com.aifinance.finance.dto.FinanceMetricResponse;
import com.aifinance.finance.dto.FinancePeriodResponse;
import com.aifinance.finance.repository.BusinessLineMetricRepository;
import com.aifinance.finance.repository.FinanceMetricRepository;
import com.aifinance.finance.repository.FinancePeriodRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinancePeriodRepository financePeriodRepository;
    private final FinanceMetricRepository financeMetricRepository;
    private final BusinessLineMetricRepository businessLineMetricRepository;

    public FinanceController(
            FinancePeriodRepository financePeriodRepository,
            FinanceMetricRepository financeMetricRepository,
            BusinessLineMetricRepository businessLineMetricRepository) {
        this.financePeriodRepository = financePeriodRepository;
        this.financeMetricRepository = financeMetricRepository;
        this.businessLineMetricRepository = businessLineMetricRepository;
    }

    @GetMapping("/periods")
    public ApiResponse<List<FinancePeriodResponse>> periods() {
        List<FinancePeriodResponse> periods = financePeriodRepository
                .findAllByOrderByYearValueAscMonthValueAsc()
                .stream()
                .map(FinancePeriodResponse::from)
                .toList();

        return ApiResponse.success(periods);
    }

    @GetMapping("/metrics")
    public ApiResponse<List<FinanceMetricResponse>> metrics(@RequestParam Long periodId) {
        List<FinanceMetricResponse> metrics = financeMetricRepository
                .findByPeriodIdOrderByMetricCodeAsc(periodId)
                .stream()
                .map(FinanceMetricResponse::from)
                .toList();

        return ApiResponse.success(metrics);
    }

    @GetMapping("/business-lines")
    public ApiResponse<List<BusinessLineMetricResponse>> businessLines(@RequestParam Long periodId) {
        List<BusinessLineMetricResponse> rows = businessLineMetricRepository
                .findByPeriodIdOrderByBusinessLineAsc(periodId)
                .stream()
                .map(BusinessLineMetricResponse::from)
                .toList();

        return ApiResponse.success(rows);
    }
}
