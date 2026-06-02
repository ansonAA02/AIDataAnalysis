package com.aifinance.alert.domain;

// Workflow status of a triggered risk alert event.
public enum AlertStatus {
    NEW,
    ACKNOWLEDGED,
    RESOLVED,
    IGNORED
}
