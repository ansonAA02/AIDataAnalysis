package com.aifinance.budget.controller;

import java.util.List;

import com.aifinance.ai.service.AiAnalysisService;
import com.aifinance.budget.dto.BudgetExplanationResponse;
import com.aifinance.budget.dto.BudgetVarianceResponse;
import com.aifinance.common.ApiResponse;
import com.aifinance.finance.service.FinanceAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final FinanceAnalysisService financeAnalysisService;
    private final AiAnalysisService aiAnalysisService;

    public BudgetController(FinanceAnalysisService financeAnalysisService, AiAnalysisService aiAnalysisService) {
        this.financeAnalysisService = financeAnalysisService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/variances")
    public ApiResponse<List<BudgetVarianceResponse>> variances(@RequestParam Long periodId) {
        List<BudgetVarianceResponse> variances = financeAnalysisService.budgetVariances(periodId)
                .stream()
                .map(BudgetVarianceResponse::from)
                .toList();

        return ApiResponse.success(variances);
    }

    @GetMapping("/explanation")
    public ApiResponse<BudgetExplanationResponse> explanation(@RequestParam Long periodId) {
        return ApiResponse.success(new BudgetExplanationResponse(periodId, aiAnalysisService.budgetExplanation(periodId)));
    }
}
