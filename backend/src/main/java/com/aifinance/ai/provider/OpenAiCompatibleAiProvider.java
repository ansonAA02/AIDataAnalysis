package com.aifinance.ai.provider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.aifinance.ai.config.AiProperties;
import com.aifinance.ai.dto.AiAnalysisContext;
import com.aifinance.ai.dto.AiAnswerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class OpenAiCompatibleAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAiProvider.class);

    private static final String SYSTEM_PROMPT = """
            你是企业经营数据分析助手。请基于用户问题和财务快照回答。
            只返回 JSON，不要返回 Markdown。JSON 字段必须包含：
            conclusion、evidence、analysis、risk、recommendation。
            对应中文含义为：结论、数据依据、原因分析、风险提示、建议动作。
            """;

    private static final String CHAT_SYSTEM_PROMPT = """
            你是企业经营数据分析助手。请基于以下财务快照，用专业、简明扼要的语言回答用户的问题。
            请直接使用 Markdown 格式回答，不要使用 JSON，也不要包含任何额外的格式包裹。
            """;

    private final ChatModel chatModel;
    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleAiProvider(ChatModel chatModel, AiProperties properties) {
        this.chatModel = chatModel;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiAnswerResponse ask(AiAnalysisContext context) {
        if (isApiKeyUnavailable()) {
            return fallbackMissingApiKey();
        }

        try {
            OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                    .model(properties.model())
                    .reasoningEffort(properties.reasoningEffort());
            Map<String, Object> extraBody = thinkingBody();
            if (!extraBody.isEmpty()) {
                optionsBuilder.extraBody(extraBody);
            }

            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage(SYSTEM_PROMPT),
                            new UserMessage(objectMapper.writeValueAsString(context))),
                    optionsBuilder.build());

            ChatResponse response = chatModel.call(prompt);
            Objects.requireNonNull(response, "AI provider response must not be null");
            String content = response.getResult().getOutput().getText();
            return objectMapper.readValue(content, AiAnswerResponse.class);
        } catch (Exception exception) {
            log.warn("AI provider request failed: {}", exception.getMessage());
            return fallbackAfterProviderError(exception);
        }
    }

    @Override
    public Flux<String> askStream(AiAnalysisContext context) {
        if (isApiKeyUnavailable()) {
            return Flux.just("⚠️ **AI 未启用**\n\n当前未配置有效的 DEEPSEEK_API_KEY，未调用大模型。\n请在项目根目录 .env 中设置真实密钥。");
        }

        try {
            // For streaming chat, we bypass the heavy "reasoning" effort to make it fast.
            // DeepSeek API allows standard model or chat model for fast responses.
            String fastModel = properties.model().contains("reasoner") ? "deepseek-chat" : properties.model();
            
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(fastModel)
                    // We don't use thinkingBody here to ensure fast streaming
                    .build();

            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage(CHAT_SYSTEM_PROMPT),
                            new UserMessage(objectMapper.writeValueAsString(context))),
                    options);

            return chatModel.stream(prompt)
                    .map(response -> {
                        if (response.getResult() != null && response.getResult().getOutput() != null) {
                            String text = response.getResult().getOutput().getText();
                            return text == null ? "" : text;
                        }
                        return "";
                    })
                    .filter(text -> !text.isEmpty())
                    .onErrorResume(e -> {
                        log.warn("AI streaming failed: {}", e.getMessage());
                        return Flux.just("\n\n⚠️ **生成失败**：调用大模型接口时发生错误 (" + e.getMessage() + ")。");
                    });
        } catch (Exception exception) {
            log.warn("AI provider stream request failed: {}", exception.getMessage());
            return Flux.just("⚠️ **系统错误**：无法初始化 AI 聊天 (" + exception.getMessage() + ")。");
        }
    }

    private boolean isApiKeyUnavailable() {
        String key = properties.apiKey();
        if (key == null) {
            return true;
        }
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        String lower = trimmed.toLowerCase();
        return lower.equals("your_deepseek_api_key_here")
                || lower.equals("changeme")
                || lower.equals("placeholder")
                || lower.equals("sk-placeholder");
    }

    private AiAnswerResponse fallbackMissingApiKey() {
        return new AiAnswerResponse(
                "AI 未启用",
                "当前未配置有效的 DEEPSEEK_API_KEY，未调用大模型。",
                "请在项目根目录 .env 或运行环境中设置真实密钥，并重启后端。驾驶舱中的金额、预算对比与风险规则仍来自数据库。",
                "在未启用 AI 期间，不会生成模型推理内容；不影响结构化财务数据查看。",
                "配置密钥后刷新页面即可使用 AI 摘要与问答。\n在此之前可先查看预算偏差表与财务报表。");
    }

    private AiAnswerResponse fallbackAfterProviderError(Throwable cause) {
        String hint = cause.getMessage() == null ? "" : cause.getMessage();
        if (hint.length() > 220) {
            hint = hint.substring(0, 220) + "...";
        }
        return new AiAnswerResponse(
                "AI 暂时不可用",
                "调用大模型接口失败。",
                "请检查 DEEPSEEK_API_KEY 是否有效、网络是否可达 DeepSeek，以及模型名称是否与账户一致。技术信息：" + hint,
                "本次未生成模型结论，请稍后重试。",
                "稍后重试或在网络正常时再次提交。\n可先使用驾驶舱中的结构化指标与图表。");
    }

    private Map<String, Object> thinkingBody() {
        if (!properties.thinkingEnabled()) {
            return Map.of();
        }

        return Map.of("thinking", Map.of("type", "enabled"));
    }
}
