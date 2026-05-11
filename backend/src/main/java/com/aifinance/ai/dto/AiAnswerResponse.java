package com.aifinance.ai.dto;

import java.io.Serializable;

public record AiAnswerResponse(
        String conclusion,
        String evidence,
        String analysis,
        String risk,
        String recommendation) implements Serializable {
}
