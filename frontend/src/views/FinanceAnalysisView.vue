<script setup lang="ts">
// Finance analysis view: subscribes to the global Pinia period store so it
// stays in sync with the top-bar selector. The local PeriodSelector and
// getPeriods() call have been removed in favor of the shared store.
import { computed, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useRoute } from 'vue-router';
import { getMetrics } from '../api/finance';
import { usePeriodStore } from '../stores/period';
import type { FinanceMetric } from '../types/finance';

// Fallback period id used when the store has not loaded periods yet
// (e.g. isolated tests mounting this view alone).
const DEFAULT_PERIOD_ID = 4;

const route = useRoute();
const periodStore = usePeriodStore();
const { currentPeriodId } = storeToRefs(periodStore);

const metrics = ref<FinanceMetric[]>([]);
const loading = ref(true);
const errorMessage = ref('');

const highlight = computed(() => (route.query.highlight as string | undefined) ?? '');

// Resolve the active period id with a safe fallback.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

// Load financial metric details for the active period.
async function load() {
  const id = resolvePeriodId();
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

// Reload when the global period selector switches periods.
watch(currentPeriodId, (next, prev) => {
  if (next === prev) return;
  void load();
});

onMounted(load);

// Apply highlight class for the row matching the ?highlight= query param.
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
