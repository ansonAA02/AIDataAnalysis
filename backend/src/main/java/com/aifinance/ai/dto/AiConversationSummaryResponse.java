package com.aifinance.ai.dto;

import java.time.Instant;

// Lightweight summary used for the conversation history list view.
public record AiConversationSummaryResponse(
        Long id,
        String title,
        Long periodId,
        boolean favorited,
        Instant createdAt,
        Instant updatedAt,
        int messageCount,
        String preview) {
}
