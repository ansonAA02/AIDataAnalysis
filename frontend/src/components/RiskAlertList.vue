<script setup lang="ts">
import type { RiskAlert } from '../types/finance';

defineProps<{
  risks: RiskAlert[];
}>();

const emit = defineEmits<{
  select: [risk: RiskAlert];
}>();
</script>

<template>
  <section class="panel-card">
    <div class="section-heading">
      <p class="eyebrow">Risk Radar</p>
      <h2>风险提醒</h2>
    </div>

    <p v-if="risks.length === 0" class="muted-text">当前期间暂无高优先级风险。</p>
    <ul v-else class="risk-list" aria-label="风险提醒列表">
      <li
        v-for="risk in risks"
        :key="risk.type"
        role="button"
        tabindex="0"
        @click="emit('select', risk)"
        @keydown.enter.prevent="emit('select', risk)"
        @keydown.space.prevent="emit('select', risk)"
      >
        <span class="risk-level" :data-level="risk.level">{{ risk.level }}</span>
        <div>
          <strong>{{ risk.type }}</strong>
          <p>{{ risk.message }}</p>
        </div>
      </li>
    </ul>
  </section>
</template>
