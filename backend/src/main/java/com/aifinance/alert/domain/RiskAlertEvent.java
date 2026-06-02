package com.aifinance.alert.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// JPA entity for a triggered risk alert event with status workflow.
@Entity
@Table(name = "risk_alert_event")
public class RiskAlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Column(name = "alert_type", nullable = false, length = 64)
    private String alertType;

    @Column(name = "alert_level", nullable = false, length = 32)
    private String alertLevel;

    @Column(name = "message", nullable = false, length = 512)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AlertStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected RiskAlertEvent() {
    }

    // Convenience constructor used when materializing a new alert from a RiskSignal.
    public RiskAlertEvent(Long periodId, String alertType, String alertLevel, String message) {
        this.periodId = periodId;
        this.alertType = alertType;
        this.alertLevel = alertLevel;
        this.message = message;
        this.status = AlertStatus.NEW;
    }

    // Stamp creation time on first persist.
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = AlertStatus.NEW;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPeriodId() {
        return periodId;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public String getMessage() {
        return message;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
