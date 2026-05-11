package com.aifinance.budget.dto;

import com.aifinance.ai.dto.AiAnswerResponse;

public record BudgetExplanationResponse(
        Long periodId,
        AiAnswerResponse explanation) {
}
