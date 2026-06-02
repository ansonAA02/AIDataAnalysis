package com.aifinance.ai.dto;

import java.time.Instant;
import java.util.List;

// Full conversation payload returned to the client when loading a session.
public record AiConversationDetailResponse(
        Long id,
        String title,
        Long periodId,
        boolean favorited,
        Instant createdAt,
        Instant updatedAt,
        List<AiMessageResponse> messages) {
}
