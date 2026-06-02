package com.aifinance.alert.dto;

import java.time.Instant;

import com.aifinance.alert.domain.RiskAlertEvent;

// Read model returned to the frontend bell dropdown and management list.
public record RiskAlertEventResponse(
        Long id,
        Long periodId,
        String alertType,
        String alertLevel,
        String message,
        String status,
        Instant createdAt,
        Instant resolvedAt) {

    // Map a RiskAlertEvent entity into the wire response DTO.
    public static RiskAlertEventResponse from(RiskAlertEvent event) {
        return new RiskAlertEventResponse(
                event.getId(),
                event.getPeriodId(),
                event.getAlertType(),
                event.getAlertLevel(),
                event.getMessage(),
                event.getStatus().name(),
                event.getCreatedAt(),
                event.getResolvedAt());
    }
}
