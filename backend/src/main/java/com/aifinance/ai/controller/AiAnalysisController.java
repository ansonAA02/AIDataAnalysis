package com.aifinance.ai.controller;

import java.util.List;

import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.dto.AiAskRequest;
import com.aifinance.ai.service.AiAnalysisService;
import com.aifinance.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/summary")
    public ApiResponse<AiAnswerResponse> summary(@RequestParam Long periodId) {
        return ApiResponse.success(aiAnalysisService.summary(periodId));
    }

    @PostMapping("/ask")
    public ApiResponse<AiAnswerResponse> ask(@RequestBody AiAskRequest request) {
        // Return AI response synchronously
        return ApiResponse.success(aiAnalysisService.ask(request));
    }

    // 新增串流回答端點
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestBody AiAskRequest request) {
        // Return SSE stream for real-time AI response
        return aiAnalysisService.askStream(request);
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<String>> suggestions(@RequestParam Long periodId) {
        return ApiResponse.success(aiAnalysisService.suggestions(periodId));
    }
}
