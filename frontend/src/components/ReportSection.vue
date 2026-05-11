<script setup lang="ts">
import type { ReportSection } from '../types/finance';

defineProps<{
  section: ReportSection;
  index: number;
}>();
</script>

<template>
  <article class="report-section-card" :style="{ '--section-index': index }">
    <div class="report-section-marker">
      {{ String(index + 1).padStart(2, '0') }}
    </div>
    <div>
      <p class="eyebrow">{{ section.code }}</p>
      <h3>{{ section.title }}</h3>
      <p>{{ section.content }}</p>
    </div>
  </article>
</template>
