package com.aifinance.ai.dto;

// Payload used to append a fresh user/assistant pair onto a conversation.
public record AppendTurnRequest(
        Long conversationId,
        Long periodId,
        String question,
        String answer) {
}
