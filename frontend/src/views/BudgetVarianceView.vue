<script setup lang="ts">
// Budget variance view: reads the active period from the global Pinia
// store and reloads variance + AI explanation whenever the user switches
// period from the global top-bar selector.
import { computed, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { getBudgetExplanation, getBudgetVariances } from '../api/budget';
import BudgetVarianceTable from '../components/BudgetVarianceTable.vue';
import VarianceExplanationCard from '../components/VarianceExplanationCard.vue';
import { usePeriodStore } from '../stores/period';
import { exportBudgetVariancesToExcel } from '../utils/exportExcel';
import type { BudgetExplanation, BudgetVariance } from '../types/finance';

// Fallback period id used only when the store has no period yet (e.g. tests
// that mount this view in isolation without preloading periods).
const DEFAULT_PERIOD_ID = 4;

const periodStore = usePeriodStore();
const { currentPeriodId } = storeToRefs(periodStore);

const loading = ref(true);
const errorMessage = ref('');
const explanationError = ref('');
const variances = ref<BudgetVariance[]>([]);
const explanation = ref<BudgetExplanation | null>(null);

// Resolve the period id to use for API calls, with a safe fallback.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

const overBudgetCount = computed(() => variances.value.filter((item) => item.varianceAmount > 0).length);
const maxVariance = computed(() =>
  variances.value.reduce<BudgetVariance | null>((current, item) => {
    if (!current || Math.abs(item.varianceRate) > Math.abs(current.varianceRate)) {
      return item;
    }

    return current;
  }, null),
);

function formatRate(value: number) {
  return `${value.toFixed(2)}%`;
}

// Trigger Excel download for the currently loaded budget variance dataset.
function handleExportExcel() {
  if (variances.value.length === 0) return;
  const periodLabel = periodStore.currentPeriod?.periodLabel ?? '';
  exportBudgetVariancesToExcel(variances.value, periodLabel);
}

// Load budget variance + AI explanation for the currently active period.
async function loadBudgetVariance() {
  const periodId = resolvePeriodId();
  loading.value = true;
  errorMessage.value = '';
  explanationError.value = '';
  explanation.value = null;

  try {
    variances.value = await getBudgetVariances(periodId);
  } catch {
    errorMessage.value = '预算偏差表加载失败，请确认后端与数据库已启动。';
    loading.value = false;
    return;
  }

  // Unblock UI rendering before fetching the slow AI explanation
  loading.value = false;

  try {
    explanation.value = await getBudgetExplanation(periodId);
  } catch {
    explanationError.value = 'AI 偏差解释暂不可用，预算表数据仍可查看。';
  }
}

// Reload data whenever the global period selector switches periods.
watch(currentPeriodId, (next, prev) => {
  if (next === prev) return;
  void loadBudgetVariance();
});

onMounted(loadBudgetVariance);
</script>

<template>
  <main class="budget-page" aria-labelledby="budget-title">
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow">Budget Variance Radar</p>
        <h2 id="budget-title">预算偏差雷达</h2>
        <p>以预算执行为核心，把超支、节余和 AI 偏差解释聚合到同一张经营雷达图景中。</p>
      </div>
      <div class="hero-actions export-actions">
        <button
          type="button"
          class="export-btn"
          :disabled="loading || variances.length === 0"
          @click="handleExportExcel"
        >
          导出 Excel
        </button>
        <div class="budget-orbit" aria-label="预算偏差概览">
          <span>{{ overBudgetCount }}</span>
          <small>项超支</small>
        </div>
      </div>
    </section>

    <section v-if="loading" class="state-card budget-loading" aria-live="polite">正在加载预算偏差...</section>
    <section v-else-if="errorMessage" class="state-card state-card--error" role="alert">
      {{ errorMessage }}
    </section>

    <template v-else>
      <p
        v-if="explanationError"
        class="state-card state-card--warning"
        role="status"
        style="margin-bottom: 1rem"
      >
        {{ explanationError }}
      </p>
      <section class="budget-signal-grid" aria-label="预算偏差信号">
        <article class="budget-signal-card">
          <span>偏差指标数</span>
          <strong>{{ variances.length }}</strong>
          <p>来自后端预算偏差 API</p>
        </article>
        <article class="budget-signal-card budget-signal-card--hot">
          <span>超支指标数</span>
          <strong>{{ overBudgetCount }}</strong>
          <p>以偏差金额大于 0 判断状态展示</p>
        </article>
        <article class="budget-signal-card">
          <span>最大偏差率</span>
          <strong>{{ maxVariance ? formatRate(maxVariance.varianceRate) : '0.00%' }}</strong>
          <p>{{ maxVariance ? maxVariance.metricName : '暂无指标' }}</p>
        </article>
      </section>

      <BudgetVarianceTable :variances="variances" />
      <VarianceExplanationCard v-if="explanation" :explanation="explanation.explanation" />
    </template>
  </main>
</template>

<style scoped>
.hero-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.export-btn {
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-ink);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.export-btn:hover:not(:disabled) {
  background: var(--color-surface-alt, #f3f4f6);
  border-color: var(--color-ink);
}

.export-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.budget-orbit {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background: var(--color-danger-bg);
  color: var(--color-danger);
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: var(--shadow-card);
}

.budget-orbit span {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.budget-orbit small {
  font-size: 12px;
  font-weight: 600;
  margin-top: 4px;
}

.budget-signal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-bottom: 24px;
  margin-top: 24px;
}

.budget-signal-card {
  padding: 24px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
}

.budget-signal-card span {
  font-size: 13px;
  color: var(--color-muted);
  font-weight: 500;
}

.budget-signal-card strong {
  font-size: 28px;
  font-family: "Geist Mono", "SF Mono", monospace;
  font-weight: 600;
  color: var(--color-ink);
  margin: 12px 0 8px;
}

.budget-signal-card p {
  margin: 0;
  font-size: 12px;
  color: var(--color-muted);
}

.budget-signal-card--hot strong {
  color: var(--color-danger);
}
</style>
