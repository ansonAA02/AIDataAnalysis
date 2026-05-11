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
    <article v-for="section in sections" :key="section.key" class="answer-section">
      <h3>{{ section.title }}</h3>
      <p>{{ answer[section.key] }}</p>
    </article>
  </section>
</template>
