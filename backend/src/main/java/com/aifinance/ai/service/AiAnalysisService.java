package com.aifinance.ai.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.aifinance.ai.dto.AiAnalysisContext;
import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.dto.AiAskRequest;
import com.aifinance.ai.provider.AiProvider;
import com.aifinance.finance.dto.FinancialSnapshot;
import com.aifinance.finance.service.FinanceAnalysisService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiAnalysisService {

    private final FinanceAnalysisService financeAnalysisService;
    private final AiProvider aiProvider;

    public AiAnalysisService(FinanceAnalysisService financeAnalysisService, AiProvider aiProvider) {
        this.financeAnalysisService = financeAnalysisService;
        this.aiProvider = aiProvider;
    }

    /**
     * Cache Pre-warming: Automatically fetch AI summaries for recent periods when the application starts.
     * This avoids the slow first-load experience for users.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preWarmAiCache() {
        System.out.println("Starting AI Cache Pre-warming...");
        
        // Fetch the 3 most recent periods
        List<FinancialSnapshot> recentSnapshots = financeAnalysisService.recentSnapshots(3);
        
        List<CompletableFuture<Void>> futures = recentSnapshots.stream()
                .map(snapshot -> CompletableFuture.runAsync(() -> {
                    System.out.println("Pre-warming AI cache for period: " + snapshot.periodLabel());
                    try {
                        // Call the cached methods to trigger actual API requests and store them in Redis
                        summary(snapshot.periodId());
                        suggestions(snapshot.periodId());
                        budgetExplanation(snapshot.periodId());
                        System.out.println("Successfully pre-warmed AI cache for period: " + snapshot.periodLabel());
                    } catch (Exception e) {
                        System.err.println("Failed to pre-warm cache for period " + snapshot.periodId() + ": " + e.getMessage());
                    }
                }))
                .toList();

        // Wait for all pre-warming tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        System.err.println("AI Cache Pre-warming finished with errors.");
                    } else {
                        System.out.println("AI Cache Pre-warming completed successfully.");
                    }
                });
    }

    @Cacheable(value = "ai-summary", key = "#periodId")
    public AiAnswerResponse summary(Long periodId) {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(periodId);
        return aiProvider.ask(new AiAnalysisContext(
                "请生成本期经营摘要，突出收入、成本、利润、现金流和主要风险。",
                snapshot));
    }

    // Interactive Q&A shouldn't be cached unless we use the question as key. Let's cache it.
    @Cacheable(value = "ai-ask", key = "#request.periodId() + '-' + #request.question()")
    public AiAnswerResponse ask(AiAskRequest request) {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(request.periodId());
        return aiProvider.ask(new AiAnalysisContext(request.question(), snapshot));
    }

    // Real-time streaming for interactive Q&A (fast model, no caching)
    public Flux<String> askStream(AiAskRequest request) {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(request.periodId());
        return aiProvider.askStream(new AiAnalysisContext(request.question(), snapshot));
    }

    @Cacheable(value = "ai-suggestions", key = "#periodId")
    public List<String> suggestions(Long periodId) {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(periodId);
        AiAnswerResponse answer = aiProvider.ask(new AiAnalysisContext(
                "请基于本期财务数据给出管理层决策建议，每条建议单独成行。",
                snapshot));

        return Arrays.stream(answer.recommendation().split("\\R"))
                .map(String::trim)
                .filter(suggestion -> !suggestion.isEmpty())
                .toList();
    }

    @Cacheable(value = "ai-budget", key = "#periodId")
    public AiAnswerResponse budgetExplanation(Long periodId) {
        FinancialSnapshot snapshot = financeAnalysisService.snapshot(periodId);
        String variances = financeAnalysisService.budgetVariances(periodId).toString();
        return aiProvider.ask(new AiAnalysisContext(
                "请解释本期预算偏差，重点说明超支或未达预算的指标。预算偏差明细：" + variances,
                snapshot));
    }
}
