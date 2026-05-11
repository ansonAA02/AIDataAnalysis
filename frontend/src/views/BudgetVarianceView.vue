<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getBudgetExplanation, getBudgetVariances } from '../api/budget';
import BudgetVarianceTable from '../components/BudgetVarianceTable.vue';
import VarianceExplanationCard from '../components/VarianceExplanationCard.vue';
import type { BudgetExplanation, BudgetVariance } from '../types/finance';

const DEFAULT_PERIOD_ID = 4;

const loading = ref(true);
const errorMessage = ref('');
const explanationError = ref('');
const variances = ref<BudgetVariance[]>([]);
const explanation = ref<BudgetExplanation | null>(null);

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

async function loadBudgetVariance() {
  loading.value = true;
  errorMessage.value = '';
  explanationError.value = '';
  explanation.value = null;

  try {
    variances.value = await getBudgetVariances(DEFAULT_PERIOD_ID);
  } catch {
    errorMessage.value = '预算偏差表加载失败，请确认后端与数据库已启动。';
    loading.value = false;
    return;
  }
  
  // Unblock UI rendering before fetching the slow AI explanation
  loading.value = false;

  try {
    explanation.value = await getBudgetExplanation(DEFAULT_PERIOD_ID);
  } catch {
    explanationError.value = 'AI 偏差解释暂不可用，预算表数据仍可查看。';
  }
}

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
      <div class="budget-orbit" aria-label="预算偏差概览">
        <span>{{ overBudgetCount }}</span>
        <small>项超支</small>
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
