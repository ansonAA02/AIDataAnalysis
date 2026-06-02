package com.aifinance.ai.dto;

import java.time.Instant;

// Single message payload used inside conversation detail responses.
public record AiMessageResponse(
        Long id,
        String role,
        String content,
        Instant createdAt) {
}
