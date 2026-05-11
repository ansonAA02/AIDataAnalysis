<script setup lang="ts">
import { ref } from 'vue';
import { askAi } from '../api/ai';
import AiAnswerPanel from '../components/AiAnswerPanel.vue';
import QuestionShortcutList from '../components/QuestionShortcutList.vue';
import type { AiAnswer } from '../types/finance';

const DEFAULT_PERIOD_ID = 4;

const shortcutQuestions = [
  '为什么本月利润下降？',
  '成本增长过快的主要原因是什么？',
  '现金流是否存在风险？',
  '本月预算偏差应该优先处理什么？',
];

const question = ref('为什么本月利润下降？');
const answer = ref<AiAnswer | null>(null);
const loading = ref(false);
const errorMessage = ref('');

async function submitQuestion(selectedQuestion = question.value) {
  const normalizedQuestion = selectedQuestion.trim();
  if (!normalizedQuestion || loading.value) {
    return;
  }

  question.value = normalizedQuestion;
  loading.value = true;
  errorMessage.value = '';

  try {
    // 預留一個空的 AI Answer 對象來接收串流數據
    answer.value = {
      conclusion: '',
      evidence: '',
      analysis: '',
      risk: '',
      recommendation: ''
    };
    
    const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '');
    
    const response = await fetch(`${apiBaseUrl}/api/ai/ask/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ periodId: DEFAULT_PERIOD_ID, question: normalizedQuestion })
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) throw new Error('No stream reader available');
    
    loading.value = false; // 開始接收數據後解除 loading 狀態
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let eventData = '';
    
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split(/\r?\n/);
      buffer = lines.pop() || '';
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          let text = line.substring(5);
          if (text.startsWith(' ')) text = text.substring(1);
          if (eventData !== '') eventData += '\n';
          eventData += text;
        } else if (line === '') {
          if (eventData !== '') {
            // 將收到的串流文字簡單地追加到 conclusion 中展示（因為串流是純文本 Markdown）
            if (answer.value) {
              answer.value.conclusion += eventData;
            }
            eventData = '';
          }
        }
      }
    }
  } catch (err) {
    console.error('AI Stream Error:', err);
    errorMessage.value = 'AI 分析請求失敗，請檢查網絡或後端配置。';
    answer.value = null;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="ai-page" aria-labelledby="ai-title">
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow">AI Finance Copilot</p>
        <h2 id="ai-title">AI 财务分析</h2>
        <p>围绕当前示例期间提问，系统会返回结论、数据依据、原因分析、风险提示和建议动作。</p>
      </div>
    </section>

    <div class="ai-workspace">
      <section class="panel-card question-panel">
        <div class="section-heading">
          <p class="eyebrow">Ask</p>
          <h2>输入经营问题</h2>
        </div>
        <form class="question-form" @submit.prevent="submitQuestion()">
          <label for="ai-question">问题</label>
          <textarea
            id="ai-question"
            v-model="question"
            rows="5"
            placeholder="例如：为什么本月利润下降？"
          ></textarea>
          <button type="submit" class="primary-button" :disabled="loading">
            {{ loading ? 'AI 正在分析...' : '提交分析' }}
          </button>
        </form>
        <p v-if="errorMessage" class="state-card state-card--error" role="alert">{{ errorMessage }}</p>
      </section>

      <QuestionShortcutList :questions="shortcutQuestions" @select="submitQuestion" />
    </div>

    <section v-if="loading" class="state-card" aria-live="polite">AI 正在分析，请稍候...</section>
    <AiAnswerPanel v-if="answer" :answer="answer" />
  </main>
</template>
