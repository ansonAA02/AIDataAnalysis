package com.aifinance.ai.dto;

import com.aifinance.finance.dto.FinancialSnapshot;

public record AiAnalysisContext(
        String question,
        FinancialSnapshot snapshot) {
}
