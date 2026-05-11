<script setup lang="ts">
import type { AiAnswer } from '../types/finance';

defineProps<{
  answer: AiAnswer;
}>();

const sections: Array<{ key: keyof AiAnswer; title: string }> = [
  { key: 'conclusion', title: '结论' },
  { key: 'evidence', title: '数据依据' },
  { key: 'analysis', title: '原因分析' },
  { key: 'risk', title: '风险提示' },
  { key: 'recommendation', title: '建议动作' },
];
</script>

<template>
  <section class="panel-card ai-answer-panel">
    <div class="section-heading">
      <p class="eyebrow">Structured Answer</p>
      <h2>AI 结构化分析</h2>
    </div>
    
    <!-- 若為串流模式（僅有 conclusion），則合併顯示 -->
    <article v-if="answer.conclusion && !answer.evidence && !answer.analysis && !answer.risk && !answer.recommendation" class="answer-section">
      <p style="white-space: pre-wrap;">{{ answer.conclusion }}</p>
    </article>
    
    <!-- 傳統結構化模式 -->
    <template v-else>
      <article v-for="section in sections" :key="section.key" class="answer-section">
        <h3 v-if="answer[section.key]">{{ section.title }}</h3>
        <p v-if="answer[section.key]">{{ answer[section.key] }}</p>
      </article>
    </template>
  </section>
</template>
