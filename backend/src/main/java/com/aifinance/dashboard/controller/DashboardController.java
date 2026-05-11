package com.aifinance.dashboard.controller;

import java.util.List;

import com.aifinance.common.ApiResponse;
import com.aifinance.dashboard.dto.DashboardOverviewResponse;
import com.aifinance.dashboard.dto.RiskAlertResponse;
import com.aifinance.dashboard.dto.TrendPointResponse;
import com.aifinance.finance.service.FinanceAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final FinanceAnalysisService financeAnalysisService;

    public DashboardController(FinanceAnalysisService financeAnalysisService) {
        this.financeAnalysisService = financeAnalysisService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> overview(@RequestParam Long periodId) {
        return ApiResponse.success(DashboardOverviewResponse.from(financeAnalysisService.snapshot(periodId)));
    }

    @GetMapping("/trends")
    public ApiResponse<List<TrendPointResponse>> trends(@RequestParam(defaultValue = "12") int months) {
        List<TrendPointResponse> trends = financeAnalysisService.recentSnapshots(months)
                .stream()
                .map(TrendPointResponse::from)
                .toList();

        return ApiResponse.success(trends);
    }

    @GetMapping("/risks")
    public ApiResponse<List<RiskAlertResponse>> risks(@RequestParam Long periodId) {
        List<RiskAlertResponse> risks = financeAnalysisService.risks(periodId)
                .stream()
                .map(RiskAlertResponse::from)
                .toList();

        return ApiResponse.success(risks);
    }
}
