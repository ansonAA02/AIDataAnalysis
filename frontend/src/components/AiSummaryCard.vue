<script setup lang="ts">
import { RouterLink } from 'vue-router';
import type { AiAnswer } from '../types/finance';

defineProps<{
  summary: AiAnswer;
  linkTo?: string;
}>();
</script>

<template>
  <RouterLink
    v-if="linkTo"
    :to="linkTo"
    class="panel-card ai-summary-card ai-summary-card--link"
    aria-label="查看 AI 分析"
  >
    <div class="section-heading">
      <p class="eyebrow">AI Insight</p>
      <h2>AI 经营摘要</h2>
    </div>
    <p class="ai-conclusion">{{ summary.conclusion }}</p>
    <dl class="ai-detail-grid">
      <div>
        <dt>数据依据</dt>
        <dd>{{ summary.evidence }}</dd>
      </div>
      <div>
        <dt>原因分析</dt>
        <dd>{{ summary.analysis }}</dd>
      </div>
      <div>
        <dt>风险提示</dt>
        <dd>{{ summary.risk }}</dd>
      </div>
      <div>
        <dt>建议动作</dt>
        <dd>{{ summary.recommendation }}</dd>
      </div>
    </dl>
  </RouterLink>
  <section v-else class="panel-card ai-summary-card">
    <div class="section-heading">
      <p class="eyebrow">AI Insight</p>
      <h2>AI 经营摘要</h2>
    </div>
    <p class="ai-conclusion">{{ summary.conclusion }}</p>
    <dl class="ai-detail-grid">
      <div>
        <dt>数据依据</dt>
        <dd>{{ summary.evidence }}</dd>
      </div>
      <div>
        <dt>原因分析</dt>
        <dd>{{ summary.analysis }}</dd>
      </div>
      <div>
        <dt>风险提示</dt>
        <dd>{{ summary.risk }}</dd>
      </div>
      <div>
        <dt>建议动作</dt>
        <dd>{{ summary.recommendation }}</dd>
      </div>
    </dl>
  </section>
</template>
