<script setup lang="ts">
import type { FinancePeriod } from '../types/finance';

defineProps<{
  periods: FinancePeriod[];
  modelValue: number | null;
}>();

const emit = defineEmits<{
  'update:modelValue': [periodId: number];
}>();

function onChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  emit('update:modelValue', Number(target.value));
}
</script>

<template>
  <label class="period-selector">
    <span>报告期间</span>
    <select :value="modelValue ?? ''" @change="onChange">
      <option v-for="period in periods" :key="period.id" :value="period.id">
        {{ period.periodLabel }}
      </option>
    </select>
  </label>
</template>
