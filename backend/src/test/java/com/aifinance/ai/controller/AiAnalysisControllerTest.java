package com.aifinance.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.aifinance.ai.dto.AiAnswerResponse;
import com.aifinance.ai.service.AiAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AiAnalysisController.class)
class AiAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    @Test
    void summaryReturnsStructuredAiAnswer() throws Exception {
        when(aiAnalysisService.summary(4L)).thenReturn(answer());

        mockMvc.perform(get("/api/ai/summary").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.conclusion").value("利润下降主要由成本异常增长导致。"))
                .andExpect(jsonPath("$.data.evidence").exists())
                .andExpect(jsonPath("$.data.analysis").exists())
                .andExpect(jsonPath("$.data.risk").exists())
                .andExpect(jsonPath("$.data.recommendation").exists());
    }

    @Test
    void askReturnsStructuredAiAnswer() throws Exception {
        when(aiAnalysisService.ask(any())).thenReturn(answer());

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "periodId": 4,
                                  "question": "为什么本月利润下降？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.conclusion").value("利润下降主要由成本异常增长导致。"));
    }

    @Test
    void suggestionsReturnsDecisionActions() throws Exception {
        when(aiAnalysisService.suggestions(4L)).thenReturn(List.of("复盘供应商成本", "提高回款优先级"));

        mockMvc.perform(get("/api/ai/suggestions").param("periodId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0]").value("复盘供应商成本"))
                .andExpect(jsonPath("$.data[1]").value("提高回款优先级"));
    }

    private AiAnswerResponse answer() {
        return new AiAnswerResponse(
                "利润下降主要由成本异常增长导致。",
                "成本从 735 万元升至 910 万元。",
                "成本上升压缩毛利空间。",
                "现金流和利润率承压。",
                "复盘供应商和项目交付成本。");
    }
}
