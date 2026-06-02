package com.aifinance.alert.dto;

// Request payload for changing the workflow status of a single risk alert event.
public record AlertStatusUpdateRequest(String status) {
}
