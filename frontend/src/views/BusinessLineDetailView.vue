<script setup lang="ts">
// Business line drill-down view: lets the user inspect ONE business line
// in depth — its KPI block, revenue/profit position vs the whole company,
// and budget variance signals filtered to that line. Reads the active
// period from the global Pinia period store so everything stays in sync.
import { computed, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { getBudgetVariances } from '../api/budget';
import { getDashboardOverview } from '../api/dashboard';
import { getBusinessLines } from '../api/finance';
import KpiCard from '../components/KpiCard.vue';
import { usePeriodStore } from '../stores/period';
import type {
  BudgetVariance,
  BusinessLineMetric,
  DashboardOverview,
} from '../types/finance';

// Fallback period id used when the store has no period yet.
const DEFAULT_PERIOD_ID = 4;

const route = useRoute();
const router = useRouter();
const periodStore = usePeriodStore();
const { currentPeriodId } = storeToRefs(periodStore);

// Reactive line name straight from the URL param (decoded by Vue Router).
const lineName = computed(() => {
  const raw = route.params.lineName;
  return Array.isArray(raw) ? raw[0] ?? '' : raw ?? '';
});

const loading = ref(true);
const errorMessage = ref('');
const lines = ref<BusinessLineMetric[]>([]);
const overview = ref<DashboardOverview | null>(null);
const variances = ref<BudgetVariance[]>([]);

// The single line matching the URL parameter.
const currentLine = computed<BusinessLineMetric | null>(() => {
  return lines.value.find((line) => line.businessLine === lineName.value) ?? null;
});

// Format currency-style numbers consistently with other views.
function formatAmount(value: number, unit: string) {
  return `${value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${unit}`;
}

// Share of total company revenue contributed by this business line.
const revenueShare = computed(() => {
  if (!currentLine.value || !overview.value || overview.value.revenue <= 0) {
    return '—';
  }
  const ratio = (currentLine.value.revenue / overview.value.revenue) * 100;
  return `${ratio.toFixed(1)}%`;
});

// Share of total company net profit contributed by this business line.
const profitShare = computed(() => {
  if (!currentLine.value || !overview.value || overview.value.netProfit <= 0) {
    return '—';
  }
  const ratio = (currentLine.value.netProfit / overview.value.netProfit) * 100;
  return `${ratio.toFixed(1)}%`;
});

// Gross margin percentage of the current business line.
const grossMargin = computed(() => {
  if (!currentLine.value || currentLine.value.revenue <= 0) {
    return '—';
  }
  const ratio = (currentLine.value.grossProfit / currentLine.value.revenue) * 100;
  return `${ratio.toFixed(1)}%`;
});

// Net margin percentage of the current business line.
const netMargin = computed(() => {
  if (!currentLine.value || currentLine.value.revenue <= 0) {
    return '—';
  }
  const ratio = (currentLine.value.netProfit / currentLine.value.revenue) * 100;
  return `${ratio.toFixed(1)}%`;
});

// Other lines used for ranking comparison.
const peerRanking = computed(() => {
  return [...lines.value]
    .sort((a, b) => b.revenue - a.revenue)
    .map((line, index) => ({ line, rank: index + 1 }));
});

const currentRank = computed(() => {
  return peerRanking.value.find((item) => item.line.businessLine === lineName.value)?.rank ?? '—';
});

// Resolve active period id with a safe fallback.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

// Load all data needed by this drill-down page in parallel.
async function loadDetail() {
  const periodId = resolvePeriodId();
  loading.value = true;
  errorMessage.value = '';
  try {
    const [linesResult, overviewResult, varianceResult] = await Promise.all([
      getBusinessLines(periodId),
      getDashboardOverview(periodId),
      getBudgetVariances(periodId),
    ]);
    lines.value = linesResult;
    overview.value = overviewResult;
    variances.value = varianceResult;
  } catch {
    errorMessage.value = '业务线详情加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
}

// Reload when the global period selector switches periods.
watch(currentPeriodId, (next, prev) => {
  if (next === prev) return;
  void loadDetail();
});

// Reload when the user navigates to a different business line.
watch(lineName, (next, prev) => {
  if (next === prev) return;
  // Data already loaded; only re-derive currentLine via computed.
  if (lines.value.length === 0) {
    void loadDetail();
  }
});

onMounted(loadDetail);

// Navigate back to the dashboard preserving period context.
function goBack() {
  router.push('/');
}
</script>

<template>
  <main class="line-detail-page" aria-labelledby="line-title">
    <header class="line-hero">
      <div>
        <p class="eyebrow">Business Line Drill-Down</p>
        <h1 id="line-title">{{ lineName || '业务线详情' }}</h1>
        <p class="muted-text">深入查看该业务线在当前期间的核心 KPI、占比和预算偏差信号。</p>
      </div>
      <button type="button" class="fin-btn fin-btn--ghost" @click="goBack">← 返回驾驶舱</button>
    </header>

    <section v-if="loading" class="state-card" aria-live="polite">正在加载业务线详情...</section>
    <section v-else-if="errorMessage" class="state-card state-card--error" role="alert">
      {{ errorMessage }}
    </section>
    <section v-else-if="!currentLine" class="state-card state-card--warning">
      未找到名为「{{ lineName }}」的业务线。可能该业务线在当前期间没有数据。
      <RouterLink to="/" class="fin-btn fin-btn--ghost" style="margin-left: 12px;">返回驾驶舱</RouterLink>
    </section>

    <template v-else>
      <!-- Top KPI strip for the current business line -->
      <section class="kpi-strip" aria-label="业务线核心指标">
        <KpiCard
          label="收入"
          :value="formatAmount(currentLine.revenue, currentLine.unit)"
          :meta="`占公司 ${revenueShare}`"
          tone="primary"
          icon="Wallet"
        />
        <KpiCard
          label="成本"
          :value="formatAmount(currentLine.cost, currentLine.unit)"
          meta="本期实际"
          tone="warning"
          icon="TrendingDown"
        />
        <KpiCard
          label="毛利"
          :value="formatAmount(currentLine.grossProfit, currentLine.unit)"
          :meta="`毛利率 ${grossMargin}`"
          tone="success"
          icon="TrendingUp"
        />
        <KpiCard
          label="净利"
          :value="formatAmount(currentLine.netProfit, currentLine.unit)"
          :meta="`占公司 ${profitShare}`"
          tone="success"
          icon="LineChart"
        />
      </section>

      <!-- Two-column layout: peer ranking + margin breakdown -->
      <section class="line-grid">
        <article class="fin-card">
          <div class="fin-card__hd">
            <p class="eyebrow">Peer Ranking</p>
            <h2 class="fin-card__title">业务线收入排名（本期）</h2>
          </div>
          <div class="fin-card__bd">
            <p class="rank-summary">
              <strong>{{ lineName }}</strong> 在 {{ peerRanking.length }} 条业务线中排名
              <span class="rank-badge">第 {{ currentRank }} 名</span>
            </p>
            <table class="fin-metric-table">
              <thead>
                <tr>
                  <th style="width: 60px;">名次</th>
                  <th>业务线</th>
                  <th>收入</th>
                  <th>净利</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="entry in peerRanking"
                  :key="entry.line.businessLine"
                  :class="entry.line.businessLine === lineName ? 'rank-row--active' : ''"
                >
                  <td>{{ entry.rank }}</td>
                  <td>
                    <RouterLink
                      v-if="entry.line.businessLine !== lineName"
                      :to="`/business-lines/${encodeURIComponent(entry.line.businessLine)}`"
                      class="line-link"
                    >
                      {{ entry.line.businessLine }}
                    </RouterLink>
                    <strong v-else>{{ entry.line.businessLine }}</strong>
                  </td>
                  <td>{{ formatAmount(entry.line.revenue, entry.line.unit) }}</td>
                  <td>{{ formatAmount(entry.line.netProfit, entry.line.unit) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="fin-card">
          <div class="fin-card__hd">
            <p class="eyebrow">Margin Breakdown</p>
            <h2 class="fin-card__title">利润率拆解</h2>
          </div>
          <div class="fin-card__bd">
            <ul class="margin-list">
              <li>
                <span>毛利率</span>
                <strong>{{ grossMargin }}</strong>
              </li>
              <li>
                <span>净利率</span>
                <strong>{{ netMargin }}</strong>
              </li>
              <li>
                <span>毛利金额</span>
                <strong>{{ formatAmount(currentLine.grossProfit, currentLine.unit) }}</strong>
              </li>
              <li>
                <span>成本占收入</span>
                <strong>
                  {{
                    currentLine.revenue > 0
                      ? `${((currentLine.cost / currentLine.revenue) * 100).toFixed(1)}%`
                      : '—'
                  }}
                </strong>
              </li>
            </ul>
            <p class="muted-text" style="margin-top: 16px; font-size: 12px;">
              对比公司整体毛利率：
              <strong>{{ overview ? `${overview.grossMargin.toFixed(1)}%` : '—' }}</strong>
              ；公司整体净利率：
              <strong>{{ overview ? `${overview.netMargin.toFixed(1)}%` : '—' }}</strong>
            </p>
          </div>
        </article>
      </section>
    </template>
  </main>
</template>

<style scoped>
.line-detail-page {
  padding: 24px 28px 40px;
}

.line-hero {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  align-items: flex-start;
}

.line-hero h1 {
  margin: 6px 0 8px;
  font-size: 1.8rem;
}

.kpi-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.line-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 20px;
}

.rank-summary {
  margin: 0 0 16px;
  font-size: 14px;
  color: var(--color-muted);
}

.rank-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 10px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 999px;
  font-weight: 700;
  font-size: 13px;
}

.rank-row--active {
  background: var(--color-primary-light, #eff6ff);
}

.rank-row--active strong {
  color: var(--color-primary);
}

.line-link {
  color: var(--color-primary);
  text-decoration: none;
  border-bottom: 1px dashed var(--color-primary);
}

.line-link:hover {
  opacity: 0.8;
}

.margin-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.margin-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
}

.margin-list li:last-child {
  border-bottom: none;
}

.margin-list span {
  color: var(--color-muted);
}

.margin-list strong {
  color: var(--color-ink);
  font-family: "Geist Mono", "SF Mono", monospace;
  font-size: 16px;
}

@media (max-width: 960px) {
  .kpi-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .line-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
