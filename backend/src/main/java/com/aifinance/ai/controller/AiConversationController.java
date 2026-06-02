package com.aifinance.ai.controller;

import java.util.List;

import com.aifinance.ai.dto.AiConversationDetailResponse;
import com.aifinance.ai.dto.AiConversationSummaryResponse;
import com.aifinance.ai.dto.AppendTurnRequest;
import com.aifinance.ai.service.AiConversationService;
import com.aifinance.common.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST endpoints that expose the persistent AI conversation history
// (list / detail / append / favorite / delete).
@RestController
@RequestMapping("/api/ai/conversations")
public class AiConversationController {

    private final AiConversationService conversationService;

    public AiConversationController(AiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // List conversations; pass favoritesOnly=true to scope to favorited rows.
    @GetMapping
    public ApiResponse<List<AiConversationSummaryResponse>> list(
            @RequestParam(value = "favoritesOnly", required = false, defaultValue = "false")
            boolean favoritesOnly) {
        return ApiResponse.success(conversationService.list(favoritesOnly));
    }

    // Load a single conversation with its full message history.
    @GetMapping("/{id}")
    public ApiResponse<AiConversationDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(conversationService.detail(id));
    }

    // Append a new (question, answer) turn to an existing or fresh conversation.
    @PostMapping("/turns")
    public ApiResponse<AiConversationDetailResponse> appendTurn(@RequestBody AppendTurnRequest request) {
        return ApiResponse.success(conversationService.appendTurn(request));
    }

    // Toggle the favorited flag and return the updated summary.
    @PostMapping("/{id}/favorite")
    public ApiResponse<AiConversationSummaryResponse> toggleFavorite(@PathVariable Long id) {
        return ApiResponse.success(conversationService.toggleFavorite(id));
    }

    // Permanently delete a conversation (and its messages via cascade).
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        conversationService.delete(id);
        return ApiResponse.success(null);
    }
}
