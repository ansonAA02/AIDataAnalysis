package com.aifinance.dashboard.dto;

import com.aifinance.finance.dto.RiskSignal;

public record RiskAlertResponse(
        String type,
        String level,
        String message) {

    public static RiskAlertResponse from(RiskSignal riskSignal) {
        return new RiskAlertResponse(
                riskSignal.type().name(),
                riskSignal.level().name(),
                riskSignal.message());
    }
}
