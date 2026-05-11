package com.aifinance.ai.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.aifinance.ai.config.AiProperties;
import com.aifinance.ai.dto.AiAnalysisContext;
import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.finance.dto.FinancialSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

class OpenAiCompatibleAiProviderTest {

    @Test
    void returnsFallbackWhenApiKeyMissingWithoutCallingModel() {
        ChatModel chatModel = Mockito.mock(ChatModel.class);
        AiProperties properties = new AiProperties(
                "   ",
                "https://api.deepseek.com",
                "deepseek-v4-pro",
                "high",
                true);
        OpenAiCompatibleAiProvider provider = new OpenAiCompatibleAiProvider(chatModel, properties);

        AiAnswerResponse answer = provider.ask(new AiAnalysisContext(
                "测试",
                new FinancialSnapshot(
                        1L, "2025-01", null, null, null, null, null, null, null, null, "万元")));

        assertThat(answer.conclusion()).isEqualTo("AI 未启用");
        verifyNoInteractions(chatModel);
    }

    @Test
    void usesSpringAiChatModelAndParsesStructuredAnswer() {
        ChatModel chatModel = Mockito.mock(ChatModel.class);
        String content = """
                {"conclusion":"利润下降主要由成本异常增长导致。","evidence":"成本从 735 万元升至 910 万元。","analysis":"成本上升压缩毛利空间。","risk":"现金流和利润率承压。","recommendation":"复盘供应商和项目交付成本。"}
                """;
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(content)))));

        AiProperties properties = new AiProperties(
                "test-key",
                "https://api.deepseek.com",
                "deepseek-v4-pro",
                "high",
                true);
        OpenAiCompatibleAiProvider provider = new OpenAiCompatibleAiProvider(chatModel, properties);

        AiAnswerResponse answer = provider.ask(new AiAnalysisContext(
                "为什么本月利润下降？",
                new FinancialSnapshot(
                        4L,
                        "2025-09",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "万元")));

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt prompt = promptCaptor.getValue();

        assertThat(prompt.getInstructions()).hasSize(2);
        assertThat(prompt.getInstructions().getFirst().getText()).contains("只返回 JSON");
        assertThat(prompt.getInstructions().get(1).getText()).contains("为什么本月利润下降？");
        assertThat(prompt.getOptions()).isInstanceOf(OpenAiChatOptions.class);

        OpenAiChatOptions options = (OpenAiChatOptions) prompt.getOptions();
        assertThat(options.getModel()).isEqualTo("deepseek-v4-pro");
        assertThat(options.getReasoningEffort()).isEqualTo("high");
        assertThat(options.getExtraBody()).containsEntry("thinking", Map.of("type", "enabled"));
        assertThat(answer.conclusion()).isEqualTo("利润下降主要由成本异常增长导致。");
        assertThat(answer.evidence()).contains("成本");
        assertThat(answer.analysis()).contains("毛利空间");
        assertThat(answer.risk()).contains("现金流");
        assertThat(answer.recommendation()).contains("复盘");
    }

    @Test
    void omitsThinkingExtraBodyWhenDisabled() {
        ChatModel chatModel = Mockito.mock(ChatModel.class);
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(
                        "{\"conclusion\":\"c\",\"evidence\":\"e\",\"analysis\":\"a\",\"risk\":\"r\",\"recommendation\":\"x\"}")))));

        AiProperties properties = new AiProperties(
                "test-key",
                "https://api.deepseek.com",
                "deepseek-v4-pro",
                "high",
                false);
        OpenAiCompatibleAiProvider provider = new OpenAiCompatibleAiProvider(chatModel, properties);
        provider.ask(new AiAnalysisContext("q", new FinancialSnapshot(
                1L, "2025-01", null, null, null, null, null, null, null, null, "万元")));

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        OpenAiChatOptions options = (OpenAiChatOptions) captor.getValue().getOptions();
        assertThat(options.getExtraBody()).isNull();
    }
}
