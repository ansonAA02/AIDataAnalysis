import * as echarts from 'echarts';
import type { EChartsType, EChartsCoreOption } from 'echarts';
import { onMounted, onUnmounted, ref, shallowRef, watch } from 'vue';

export function useECharts(getOption: (ec: typeof echarts) => EChartsCoreOption) {
  const chartRef = ref<HTMLElement | null>(null);
  const chartInstance = shallowRef<EChartsType | null>(null);

  function renderChart() {
    if (!chartRef.value) return;

    if (!chartInstance.value) {
      chartInstance.value = echarts.init(chartRef.value);
    }

    const option = getOption(echarts);
    chartInstance.value.setOption(option);
  }

  function handleResize() {
    chartInstance.value?.resize();
  }

  onMounted(() => {
    renderChart();
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    chartInstance.value?.dispose();
    chartInstance.value = null;
  });

  return {
    chartRef,
    chartInstance,
    renderChart,
  };
}