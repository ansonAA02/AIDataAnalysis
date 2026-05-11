package com.aifinance.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.aifinance.ai.dto.AiAnalysisContext;
import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.dto.AiAskRequest;
import com.aifinance.ai.provider.AiProvider;
import com.aifinance.finance.dto.FinancialSnapshot;
import com.aifinance.finance.service.FinanceAnalysisService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AiAnalysisServiceTest {

    @Test
    void summaryBuildsBusinessSummaryQuestionWithCurrentFinancialSnapshot() {
        FinanceAnalysisService financeAnalysisService = Mockito.mock(FinanceAnalysisService.class);
        FinancialSnapshot snapshot = snapshot();
        when(financeAnalysisService.snapshot(4L)).thenReturn(snapshot);
        CapturingAiProvider aiProvider = new CapturingAiProvider(answer());
        AiAnalysisService service = new AiAnalysisService(financeAnalysisService, aiProvider);

        AiAnswerResponse response = service.summary(4L);

        assertThat(response.conclusion()).isEqualTo("利润下降主要由成本异常增长导致。");
        assertThat(aiProvider.context().question()).contains("经营摘要");
        assertThat(aiProvider.context().snapshot()).isEqualTo(snapshot);
    }

    @Test
    void askUsesUserQuestionAndCurrentFinancialSnapshot() {
        FinanceAnalysisService financeAnalysisService = Mockito.mock(FinanceAnalysisService.class);
        FinancialSnapshot snapshot = snapshot();
        when(financeAnalysisService.snapshot(4L)).thenReturn(snapshot);
        CapturingAiProvider aiProvider = new CapturingAiProvider(answer());
        AiAnalysisService service = new AiAnalysisService(financeAnalysisService, aiProvider);

        AiAnswerResponse response = service.ask(new AiAskRequest(4L, "为什么本月利润下降？"));

        assertThat(response.analysis()).contains("毛利空间");
        assertThat(aiProvider.context().question()).contains("为什么本月利润下降？");
        assertThat(aiProvider.context().snapshot().periodId()).isEqualTo(4L);
    }

    @Test
    void suggestionsReturnsActionListFromAiRecommendation() {
        FinanceAnalysisService financeAnalysisService = Mockito.mock(FinanceAnalysisService.class);
        when(financeAnalysisService.snapshot(4L)).thenReturn(snapshot());
        AiAnswerResponse answer = new AiAnswerResponse(
                "需要控制成本。",
                "成本超预算。",
                "供应商价格上涨。",
                "现金流承压。",
                "复盘供应商成本\n收紧非必要支出\n提高回款优先级");
        AiAnalysisService service = new AiAnalysisService(financeAnalysisService, new CapturingAiProvider(answer));

        List<String> suggestions = service.suggestions(4L);

        assertThat(suggestions).containsExactly("复盘供应商成本", "收紧非必要支出", "提高回款优先级");
    }

    private FinancialSnapshot snapshot() {
        return new FinancialSnapshot(
                4L,
                "2025-09",
                new BigDecimal("1250.00"),
                new BigDecimal("910.00"),
                new BigDecimal("340.00"),
                new BigDecimal("185.00"),
                new BigDecimal("155.00"),
                new BigDecimal("-80.00"),
                new BigDecimal("27.20"),
                new BigDecimal("12.40"),
                "万元");
    }

    private AiAnswerResponse answer() {
        return new AiAnswerResponse(
                "利润下降主要由成本异常增长导致。",
                "成本从 735 万元升至 910 万元。",
                "成本上升压缩毛利空间。",
                "现金流和利润率承压。",
                "复盘供应商和项目交付成本。");
    }

    private static class CapturingAiProvider implements AiProvider {

        private final AiAnswerResponse response;
        private final AtomicReference<AiAnalysisContext> context = new AtomicReference<>();

        private CapturingAiProvider(AiAnswerResponse response) {
            this.response = response;
        }

        @Override
        public AiAnswerResponse ask(AiAnalysisContext context) {
            this.context.set(context);
            return response;
        }

        @Override
        public reactor.core.publisher.Flux<String> askStream(AiAnalysisContext context) {
            this.context.set(context);
            return reactor.core.publisher.Flux.just(response.conclusion());
        }

        private AiAnalysisContext context() {
            return context.get();
        }
    }
}
