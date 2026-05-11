package com.aifinance.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ai.deepseek")
public record AiProperties(
        @DefaultValue("") String apiKey,
        @DefaultValue("https://api.deepseek.com") String baseUrl,
        @DefaultValue("deepseek-v4-pro") String model,
        @DefaultValue("high") String reasoningEffort,
        @DefaultValue("true") boolean thinkingEnabled) {
}
