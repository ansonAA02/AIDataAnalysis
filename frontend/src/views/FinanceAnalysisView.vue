<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getMetrics, getPeriods } from '../api/finance';
import PeriodSelector from '../components/PeriodSelector.vue';
import type { FinanceMetric, FinancePeriod } from '../types/finance';

const route = useRoute();
const periods = ref<FinancePeriod[]>([]);
const selectedPeriodId = ref<number | null>(null);
const metrics = ref<FinanceMetric[]>([]);
const loading = ref(true);
const errorMessage = ref('');

const highlight = computed(() => (route.query.highlight as string | undefined) ?? '');

async function load() {
  const id = selectedPeriodId.value;
  if (id == null) {
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  try {
    metrics.value = await getMetrics(id);
  } catch {
    errorMessage.value = '指标加载失败。';
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    periods.value = await getPeriods();
    const list = periods.value;
    selectedPeriodId.value = (list.length ? list[list.length - 1] : undefined)?.id ?? 4;
  } catch {
    errorMessage.value = '无法加载期间。';
    selectedPeriodId.value = 4;
  }
  await load();
});

watch(selectedPeriodId, (id, prev) => {
  if (id != null && prev != null && id !== prev) {
    void load();
  }
});

function rowClass(code: string) {
  return highlight.value && highlight.value === code ? 'fin-row-highlight' : '';
}
</script>

<template>
  <main class="finance-page page-surface">
    <header class="page-hero">
      <div>
        <p class="eyebrow">Finance Analysis</p>
        <h1>财务指标明细</h1>
        <p class="muted-text">按期间查看内置示例数据中的核心指标，可与预算偏差、AI 分析交叉使用。</p>
      </div>
      <PeriodSelector
        v-if="periods.length && selectedPeriodId != null"
        :periods="periods"
        :model-value="selectedPeriodId"
        @update:model-value="(id) => (selectedPeriodId = id)"
      />
    </header>

    <section v-if="loading" class="state-card">加载中…</section>
    <section v-else-if="errorMessage" class="state-card state-card--error" role="alert">{{ errorMessage }}</section>
    <section v-else class="fin-card">
      <table class="fin-metric-table" style="margin: 0">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>分类</th>
            <th>实际值</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in metrics" :key="m.metricCode" :class="rowClass(m.metricCode)">
            <td>{{ m.metricCode }}</td>
            <td>{{ m.metricName }}</td>
            <td>{{ m.metricCategory }}</td>
            <td>{{ m.actualValue.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }} {{ m.unit }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>

<style scoped>
.page-surface {
  padding: 24px 28px 40px;
}

.page-hero {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-hero h1 {
  margin: 6px 0 8px;
}

.fin-row-highlight {
  background: #fff9e6;
}
</style>
