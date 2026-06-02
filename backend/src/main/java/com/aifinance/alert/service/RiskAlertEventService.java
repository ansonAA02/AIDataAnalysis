package com.aifinance.alert.service;

import java.time.Instant;
import java.util.List;

import com.aifinance.alert.domain.AlertStatus;
import com.aifinance.alert.domain.RiskAlertEvent;
import com.aifinance.alert.dto.RiskAlertEventResponse;
import com.aifinance.alert.repository.RiskAlertEventRepository;
import com.aifinance.finance.dto.RiskSignal;
import com.aifinance.finance.service.FinanceAnalysisService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service that materializes RiskSignal computations into persistent
// risk_alert_event rows and exposes workflow operations on them.
@Service
public class RiskAlertEventService {

    private final RiskAlertEventRepository repository;
    private final FinanceAnalysisService financeAnalysisService;

    public RiskAlertEventService(
            RiskAlertEventRepository repository,
            FinanceAnalysisService financeAnalysisService) {
        this.repository = repository;
        this.financeAnalysisService = financeAnalysisService;
    }

    // Compute current RiskSignals for a period and upsert them as alert events,
    // returning the persisted view so the UI can immediately render with status.
    @Transactional
    public List<RiskAlertEventResponse> syncForPeriod(Long periodId) {
        List<RiskSignal> signals = financeAnalysisService.risks(periodId);
        for (RiskSignal signal : signals) {
            String type = signal.type().name();
            String level = signal.level().name();
            String message = signal.message();
            // Idempotent insert: re-trigger does not create duplicate events.
            // Catch unique-key races (e.g. legacy rows with mojibake message
            // that the JPA finder cannot match exactly) and treat as already-present.
            if (repository.findByPeriodIdAndAlertTypeAndMessage(periodId, type, message).isEmpty()) {
                try {
                    repository.save(new RiskAlertEvent(periodId, type, level, message));
                } catch (DataIntegrityViolationException ignored) {
                    // Existing row collides on (period_id, alert_type, message) — skip.
                }
            }
        }
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(RiskAlertEventResponse::from)
                .toList();
    }

    // Return only unread alerts for the bell dropdown.
    @Transactional(readOnly = true)
    public List<RiskAlertEventResponse> listUnread() {
        return repository.findAllByStatusOrderByCreatedAtDesc(AlertStatus.NEW).stream()
                .map(RiskAlertEventResponse::from)
                .toList();
    }

    // Return every alert in newest-first order for the management view.
    @Transactional(readOnly = true)
    public List<RiskAlertEventResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(RiskAlertEventResponse::from)
                .toList();
    }

    // Count unread alerts for the red dot badge.
    @Transactional(readOnly = true)
    public long countUnread() {
        return repository.countByStatus(AlertStatus.NEW);
    }

    // Update workflow status; stamps resolvedAt for terminal states.
    @Transactional
    public RiskAlertEventResponse updateStatus(Long id, String statusName) {
        RiskAlertEvent event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
        AlertStatus next;
        try {
            next = AlertStatus.valueOf(statusName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported alert status: " + statusName);
        }
        event.setStatus(next);
        if (next == AlertStatus.RESOLVED || next == AlertStatus.IGNORED) {
            event.setResolvedAt(Instant.now());
        } else {
            event.setResolvedAt(null);
        }
        return RiskAlertEventResponse.from(repository.save(event));
    }

    // Mark every NEW alert as ACKNOWLEDGED in one shot (used by "mark all read").
    @Transactional
    public int markAllRead() {
        List<RiskAlertEvent> unread = repository.findAllByStatusOrderByCreatedAtDesc(AlertStatus.NEW);
        for (RiskAlertEvent event : unread) {
            event.setStatus(AlertStatus.ACKNOWLEDGED);
        }
        repository.saveAll(unread);
        return unread.size();
    }
}
