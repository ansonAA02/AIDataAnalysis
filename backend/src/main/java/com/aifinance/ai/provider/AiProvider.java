package com.aifinance.ai.provider;

import com.aifinance.ai.dto.AiAnalysisContext;
import com.aifinance.ai.dto.AiAnswerResponse;
import reactor.core.publisher.Flux;

public interface AiProvider {

    AiAnswerResponse ask(AiAnalysisContext context);

    Flux<String> askStream(AiAnalysisContext context);
}
