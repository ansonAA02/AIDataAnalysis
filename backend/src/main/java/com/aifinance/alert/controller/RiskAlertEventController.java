package com.aifinance.alert.controller;

import java.util.List;
import java.util.Map;

import com.aifinance.alert.dto.AlertStatusUpdateRequest;
import com.aifinance.alert.dto.RiskAlertEventResponse;
import com.aifinance.alert.service.RiskAlertEventService;
import com.aifinance.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST endpoints for managing risk alert events
// (sync from snapshot, list unread/all, count, status update, mark all read).
@RestController
@RequestMapping("/api/alerts")
public class RiskAlertEventController {

    private final RiskAlertEventService service;

    public RiskAlertEventController(RiskAlertEventService service) {
        this.service = service;
    }

    // Recompute alerts for the given period and persist any new ones.
    @PostMapping("/sync")
    public ApiResponse<List<RiskAlertEventResponse>> sync(@RequestParam Long periodId) {
        return ApiResponse.success(service.syncForPeriod(periodId));
    }

    // List events filtered by readiness state ("unread" by default, "all" otherwise).
    @GetMapping
    public ApiResponse<List<RiskAlertEventResponse>> list(
            @RequestParam(name = "scope", defaultValue = "unread") String scope) {
        if ("all".equalsIgnoreCase(scope)) {
            return ApiResponse.success(service.listAll());
        }
        return ApiResponse.success(service.listUnread());
    }

    // Count of unread alerts for the bell badge.
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success(Map.of("count", service.countUnread()));
    }

    // Update the workflow status of a single alert event.
    @PatchMapping("/{id}/status")
    public ApiResponse<RiskAlertEventResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AlertStatusUpdateRequest request) {
        return ApiResponse.success(service.updateStatus(id, request.status()));
    }

    // Mark every unread alert as acknowledged in one call.
    @PostMapping("/mark-all-read")
    public ApiResponse<Map<String, Integer>> markAllRead() {
        return ApiResponse.success(Map.of("updated", service.markAllRead()));
    }
}
