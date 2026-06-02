package com.aifinance.alert.repository;

import java.util.List;
import java.util.Optional;

import com.aifinance.alert.domain.AlertStatus;
import com.aifinance.alert.domain.RiskAlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA repository for risk alert events.
public interface RiskAlertEventRepository extends JpaRepository<RiskAlertEvent, Long> {

    // Lookup an existing alert by its natural identity (period + type + message)
    // so we never create duplicates when the same risk re-triggers.
    Optional<RiskAlertEvent> findByPeriodIdAndAlertTypeAndMessage(
            Long periodId, String alertType, String message);

    // List unread alerts (status = NEW) in newest-first order for the bell dropdown.
    List<RiskAlertEvent> findAllByStatusOrderByCreatedAtDesc(AlertStatus status);

    // List every alert in newest-first order for the management view.
    List<RiskAlertEvent> findAllByOrderByCreatedAtDesc();

    // Count unread alerts to drive the red dot badge.
    long countByStatus(AlertStatus status);
}
