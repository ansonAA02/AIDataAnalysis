<script setup lang="ts">
import { watch } from 'vue';
import { useECharts } from '../composables/useECharts';
import type { BusinessLineMetric } from '../types/finance';

const props = defineProps<{
  data: BusinessLineMetric[];
}>();

const { chartRef, renderChart } = useECharts((echarts) => {
  if (!props.data || props.data.length === 0) return {};

  const style = getComputedStyle(document.documentElement);
  const colors = [
    style.getPropertyValue('--color-primary').trim() || '#2563EB',
    style.getPropertyValue('--color-success').trim() || '#10B981',
    style.getPropertyValue('--color-warning').trim() || '#F59E0B',
    style.getPropertyValue('--color-accent-purple').trim() || '#8B5CF6'
  ];

  return {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '0',
      left: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: {
        fontSize: 12,
        color: style.getPropertyValue('--color-muted').trim() || '#64748B'
      }
    },
    series: [
      {
        name: '收入占比',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: { show: false },
        data: props.data.map((d, i) => ({
          value: d.revenue,
          name: d.businessLine,
          itemStyle: { color: colors[i % colors.length] }
        }))
      }
    ]
  };
});

watch(() => props.data, () => {
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