package com.aifinance.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Helps verify that {@code DEEPSEEK_API_KEY} reached the JVM (common issue: Docker container
 * not recreated after editing {@code .env}).
 */
@Component
public class AiStartupDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(AiStartupDiagnostics.class);

    private final AiProperties properties;

    public AiStartupDiagnostics(AiProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logAiConfiguration() {
        String key = properties.apiKey();
        if (key == null || key.isBlank()) {
            log.warn("DEEPSEEK_API_KEY is empty: AI calls will use the local fallback message. "
                    + "If you use Docker, set the key in project-root .env then run: "
                    + "docker compose --profile dev up -d --force-recreate backend-dev "
                    + "(restart alone does not reload env).");
            return;
        }
        String trimmed = key.trim();
        log.info("DEEPSEEK_API_KEY is set (length={} chars). Base URL: {}. Model: {}.",
                trimmed.length(),
                properties.baseUrl(),
                properties.model());
    }
}
