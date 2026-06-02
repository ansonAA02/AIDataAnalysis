<script setup lang="ts">
// AI analysis view: reads the active period from the global Pinia store so
// every AI question is scoped to whichever period the user picked in the
// top-bar selector.
import { ref } from 'vue';
import { storeToRefs } from 'pinia';
import { streamAiAnswer } from '../api/ai';
import AiAnswerPanel from '../components/AiAnswerPanel.vue';
import QuestionShortcutList from '../components/QuestionShortcutList.vue';
import { usePeriodStore } from '../stores/period';
import type { AiAnswer } from '../types/finance';

// Fallback period id for isolated test mounts without a preloaded store.
const DEFAULT_PERIOD_ID = 4;

const periodStore = usePeriodStore();
const { currentPeriodId } = storeToRefs(periodStore);

// Resolve the active period id with a safe fallback.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

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
    answer.value = {
      conclusion: '',
      evidence: '',
      analysis: '',
      risk: '',
      recommendation: ''
    };

    await streamAiAnswer(
      { periodId: resolvePeriodId(), question: normalizedQuestion },
      (chunk) => {
        loading.value = false;
        if (answer.value) {
          answer.value.conclusion += chunk;
        }
      }
    );
  } catch (err) {
    console.error('AI Stream Error:', err);
    errorMessage.value = 'AI 分析请求失败，请检查网络或后端配置。';
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
