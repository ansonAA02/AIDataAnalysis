package com.aifinance.finance.dto;

import com.aifinance.finance.domain.RiskLevel;
import com.aifinance.finance.domain.RiskType;

public record RiskSignal(
        RiskType type,
        RiskLevel level,
        String message) {
}
