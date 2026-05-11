<script setup lang="ts">
import { watch } from 'vue';
import { useECharts } from '../composables/useECharts';
import type { TrendPoint } from '../types/finance';

const props = defineProps<{
  trends: TrendPoint[];
}>();

const { chartRef, renderChart } = useECharts((echarts) => {
  if (!props.trends || props.trends.length === 0) return {};

  // Get CSS variables to respect the minimalist theme
  const style = getComputedStyle(document.documentElement);
  const revenue = style.getPropertyValue('--color-chart-revenue').trim() || '#2563EB';
  const cost = style.getPropertyValue('--color-chart-cost').trim() || '#F59E0B';
  const profit = style.getPropertyValue('--color-chart-profit').trim() || '#10B981';
  const cash = style.getPropertyValue('--color-chart-cash').trim() || '#8B5CF6';
  const border = style.getPropertyValue('--color-border').trim() || '#E2E8F0';
  const ink = style.getPropertyValue('--color-ink').trim() || '#0F172A';
  const muted = style.getPropertyValue('--color-muted').trim() || '#64748B';
  const surface = style.getPropertyValue('--color-surface').trim() || '#FFFFFF';

  // Define ECharts configuration with high-end minimalist theme
  return {
    // Set minimalist editorial colors
    color: [revenue, cost, profit, cash],
    tooltip: { 
      trigger: 'axis',
      backgroundColor: surface,
      borderColor: border,
      textStyle: { color: ink },
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-radius: 8px;'
    },
    legend: {
      top: 0,
      textStyle: { color: muted, fontSize: 11 }, // Muted legend text
      itemWidth: 10,
      itemHeight: 10,
    },
    grid: {
      top: 40,
      left: 48,
      right: 20,
      bottom: 36,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.trends.map((point) => point.periodLabel),
      axisLine: { lineStyle: { color: border } },
      axisLabel: { color: muted, fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '{value}', color: muted, fontSize: 11 },
      splitLine: { lineStyle: { color: border, type: 'dashed' } },
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: 0.25,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { width: 2 },
        data: props.trends.map((point) => point.revenue),
      },
      {
        name: '成本',
        type: 'line',
        smooth: 0.25,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { width: 2 },
        data: props.trends.map((point) => point.cost),
      },
      {
        name: '净利',
        type: 'line',
        smooth: 0.25,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { width: 2 },
        data: props.trends.map((point) => point.netProfit),
      },
      {
        name: '经营现金流',
        type: 'line',
        smooth: 0.25,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { width: 2 },
        data: props.trends.map((point) => point.operatingCashFlow),
      },
    ],
  };
});

watch(() => props.trends, () => {
  renderChart();
}, { deep: true });
</script>

<template>
  <section class="fin-card fin-trend-card">
    <div class="section-heading">
      <p class="eyebrow">12-Month Trend</p>
      <h2>收入 · 成本 · 净利 · 现金流</h2>
    </div>
    <div ref="chartRef" class="trend-chart" role="img" aria-label="近十二个月收入、成本、净利与经营现金流趋势"></div>
  </section>
</template>

<style scoped>
.fin-trend-card {
  border-radius: 12px;
}
</style>
