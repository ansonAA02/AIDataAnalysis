<script setup lang="ts">
import { watch } from 'vue';
import { useECharts } from '../composables/useECharts';
import type { TrendPoint } from '../types/finance';

const props = defineProps<{
  trends: TrendPoint[];
}>();

const { chartRef, renderChart } = useECharts((echarts) => {
  if (!props.trends || props.trends.length === 0) return {};

  const style = getComputedStyle(document.documentElement);
  const primary = style.getPropertyValue('--color-primary').trim() || '#2563EB';
  const muted = style.getPropertyValue('--color-muted').trim() || '#64748B';
  const border = style.getPropertyValue('--color-border').trim() || '#E2E8F0';

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      top: 20,
      left: 48,
      right: 20,
      bottom: 24,
    },
    xAxis: {
      type: 'category',
      data: props.trends.map(t => t.periodLabel),
      axisLine: { lineStyle: { color: border } },
      axisLabel: { color: muted, fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: muted, fontSize: 11 },
      splitLine: { lineStyle: { color: border, type: 'dashed' } },
    },
    series: [
      {
        name: '实际成本',
        type: 'bar',
        barWidth: '40%',
        itemStyle: { color: primary, borderRadius: [4, 4, 0, 0] },
        data: props.trends.map(t => t.cost),
      }
    ],
  };
});

watch(() => props.trends, () => {
  renderChart();
}, { deep: true });
</script>

<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 240px;
}
</style>