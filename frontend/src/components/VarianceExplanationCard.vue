<script setup lang="ts">
import type { AiAnswer } from '../types/finance';

defineProps<{
  explanation: AiAnswer;
}>();
</script>

<template>
  <section class="panel-card variance-explanation-card">
    <div class="section-heading">
      <p class="eyebrow">AI Diagnosis</p>
      <h2>AI 偏差解释</h2>
    </div>
    <p class="variance-diagnosis">{{ explanation.conclusion }}</p>
    <div class="variance-explanation-grid">
      <article>
        <span>数据依据</span>
        <p>{{ explanation.evidence }}</p>
      </article>
      <article>
        <span>原因分析</span>
        <p>{{ explanation.analysis }}</p>
      </article>
      <article>
        <span>风险提示</span>
        <p>{{ explanation.risk }}</p>
      </article>
      <article>
        <span>建议动作</span>
        <p>{{ explanation.recommendation }}</p>
      </article>
    </div>
  </section>
</template>
