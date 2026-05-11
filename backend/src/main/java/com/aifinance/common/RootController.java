package com.aifinance.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backend is API-only; there is no HTML UI on port 8080.
 * Browsers opening {@code /} otherwise receive 404 and the Whitelabel page.
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ApiResponse<Map<String, String>> root() {
        return ApiResponse.success(Map.of(
                "service", "ai-finance-backend",
                "docs", "REST API under /api/**",
                "health", "/api/health",
                "frontend", "Run Vite dev server (e.g. http://localhost:5173) for the web UI."));
    }
}
