<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { getAiSummary } from '../api/ai';
import { getBudgetVariances } from '../api/budget';
import { getDashboardOverview, getDashboardRisks, getDashboardTrends } from '../api/dashboard';
import { getBusinessLines, getPeriods } from '../api/finance';
import AiSummaryCard from '../components/AiSummaryCard.vue';
import BudgetBarChart from '../components/BudgetBarChart.vue';
import RevenueDonutChart from '../components/RevenueDonutChart.vue';
import KpiCard from '../components/KpiCard.vue';
import PeriodSelector from '../components/PeriodSelector.vue';
import RiskAlertList from '../components/RiskAlertList.vue';
import TrendChart from '../components/TrendChart.vue';
import type {
  AiAnswer,
  BudgetVariance,
  BusinessLineMetric,
  DashboardOverview,
  FinancePeriod,
  RiskAlert,
  TrendPoint,
} from '../types/finance';

const periods = ref<FinancePeriod[]>([]);
const selectedPeriodId = ref<number | null>(null);

const loading = ref(true);
const errorMessage = ref('');
const aiErrorMessage = ref('');
const overview = ref<DashboardOverview | null>(null);
const trends = ref<TrendPoint[]>([]);
const risks = ref<RiskAlert[]>([]);
const aiSummary = ref<AiAnswer | null>(null);
const businessLines = ref<BusinessLineMetric[]>([]);
const variances = ref<BudgetVariance[]>([]);

const selectedRisk = ref<RiskAlert | null>(null);

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return '早上好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const budgetHealthLabel = computed(() => {
  if (variances.value.length === 0) {
    return '—';
  }
  const scores = variances.value.map((row) => {
    const rate = Math.abs(row.varianceRate);
    return Math.max(0, 100 - rate);
  });
  const avg = scores.reduce((a, b) => a + b, 0) / scores.length;
  return `${avg.toFixed(1)}%`;
});

const topAnomalies = computed(() =>
  [...variances.value]
    .sort((a, b) => Math.abs(b.varianceRate) - Math.abs(a.varianceRate))
    .slice(0, 3),
);

function formatAmount(value: number, unit: string) {
  return `${value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${unit}`;
}

function formatPercent(value: number) {
  return `${value.toFixed(2)}%`;
}

function lineShare(line: BusinessLineMetric) {
  if (!overview.value || overview.value.revenue <= 0) {
    return '—';
  }
  return `${((line.revenue / overview.value.revenue) * 100).toFixed(1)}%`;
}

async function loadDashboard() {
  const periodId = selectedPeriodId.value;
  if (periodId == null) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  aiErrorMessage.value = '';
  aiSummary.value = null;

  try {
    const [overviewResult, trendsResult, risksResult, linesResult, varianceResult] = await Promise.all([
      getDashboardOverview(periodId),
      getDashboardTrends(12),
      getDashboardRisks(periodId),
      getBusinessLines(periodId),
      getBudgetVariances(periodId),
    ]);

    overview.value = overviewResult;
    trends.value = trendsResult;
    risks.value = risksResult;
    businessLines.value = linesResult;
    variances.value = varianceResult;
  } catch {
    errorMessage.value = '经营数据加载失败，请稍后重试。';
    overview.value = null;
    return;
  } finally {
    loading.value = false;
  }

  try {
    aiSummary.value = await getAiSummary(periodId);
  } catch {
    aiErrorMessage.value = 'AI 经营摘要暂不可用，请确认后端 DEEPSEEK_API_KEY 已配置。';
  }
}

onMounted(async () => {
  try {
    periods.value = await getPeriods();
    const list = periods.value;
    const last = list.length ? list[list.length - 1] : undefined;
    selectedPeriodId.value = last?.id ?? 4;
  } catch {
    errorMessage.value = '无法加载财务期间列表。';
    selectedPeriodId.value = 4;
  }
  await loadDashboard();
});

watch(selectedPeriodId, (id, previous) => {
  if (id == null || previous == null || id === previous) {
    return;
  }
  void loadDashboard();
});

function onPeriodChange(id: number) {
  selectedPeriodId.value = id;
}

function onRiskSelect(risk: RiskAlert) {
  selectedRisk.value = risk;
}

function closeRiskDialog() {
  selectedRisk.value = null;
}
</script>

<template>
  <main class="fin-dashboard" aria-labelledby="dash-heading">
    <header class="fin-header">
      <div class="fin-header__greeting">
        <p>{{ greeting }}，欢迎回到经营驾驶舱</p>
        <h1 id="dash-heading">本月经营概览</h1>
      </div>
      <div class="fin-header__actions">
        <PeriodSelector
          v-if="periods.length > 0 && selectedPeriodId != null"
          :periods="periods"
          :model-value="selectedPeriodId"
          @update:model-value="onPeriodChange"
        />
        <RouterLink class="fin-btn fin-btn--primary" to="/reports">生成报告</RouterLink>
        <RouterLink class="fin-btn fin-btn--accent" to="/ai">AI 问答</RouterLink>
        <div class="fin-avatar" aria-hidden="true">ME</div>
      </div>
    </header>

    <section v-if="loading" class="state-card fin-card" aria-live="polite">正在加载经营数据...</section>
    <section v-else-if="errorMessage" class="state-card state-card--error fin-card" role="alert">
      {{ errorMessage }}
    </section>

    <template v-else-if="overview">
      <div class="fin-layout">
        <div class="fin-main-stack">
          <section class="fin-card" aria-labelledby="overview-title">
            <div class="fin-card__hd">
              <p class="eyebrow">Overview</p>
              <h2 id="overview-title">{{ overview.periodLabel }} 核心指标</h2>
            </div>
            <div class="fin-card__bd">
              <div class="fin-kpi-row">
                <KpiCard
                  label="收入"
                  :value="formatAmount(overview.revenue, overview.unit)"
                  meta="本期实际"
                  tone="primary"
                  icon="Wallet"
                  :to="{ path: '/finance', query: { highlight: 'REVENUE' } }"
                />
                <KpiCard
                  label="成本"
                  :value="formatAmount(overview.cost, overview.unit)"
                  meta="本期实际"
                  tone="warning"
                  icon="TrendingDown"
                  to="/budget"
                />
                <KpiCard
                  label="毛利"
                  :value="formatAmount(overview.grossProfit, overview.unit)"
                  meta="本期实际"
                  tone="success"
                  icon="TrendingUp"
                  to="/budget"
                />
                <KpiCard
                  label="净利"
                  :value="formatAmount(overview.netProfit, overview.unit)"
                  meta="本期实际"
                  tone="success"
                  icon="LineChart"
                  to="/ai"
                />
                <KpiCard
                  label="经营现金流"
                  :value="formatAmount(overview.operatingCashFlow, overview.unit)"
                  meta="本期实际"
                  tone="primary"
                  icon="Banknote"
                  :to="{ path: '/finance', query: { highlight: 'OPERATING_CASH_FLOW' } }"
                />
              </div>
            </div>
          </section>

          <div class="chart-grid">
            <section class="fin-card">
              <div class="fin-card__hd">
                <p class="eyebrow">Trends</p>
                <h2 class="fin-card__title">核心指标 12 个月趋势</h2>
              </div>
              <div class="fin-card__bd">
                <TrendChart :trends="trends" />
              </div>
            </section>
            
            <section class="fin-card">
              <div class="fin-card__hd">
                <p class="eyebrow">Budget</p>
                <h2 class="fin-card__title">实际成本趋势</h2>
              </div>
              <div class="fin-card__bd">
                <BudgetBarChart :trends="trends" />
              </div>
            </section>
          </div>

          <section class="fin-bottom-grid" aria-label="扩展分析">
            <div class="fin-card">
              <div class="fin-card__hd">
                <p class="eyebrow">Business mix</p>
                <h2 class="fin-card__title">业务线贡献</h2>
              </div>
              <div class="fin-card__bd">
                <RevenueDonutChart :data="businessLines" />
                <table class="fin-metric-table" style="margin-top: 16px;">
                  <thead>
                    <tr>
                      <th>业务线</th>
                      <th>收入</th>
                      <th>净利</th>
                      <th>占收入比</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="line in businessLines" :key="line.businessLine">
                      <td>{{ line.businessLine }}</td>
                      <td>{{ formatAmount(line.revenue, line.unit) }}</td>
                      <td>{{ formatAmount(line.netProfit, line.unit) }}</td>
                      <td>{{ lineShare(line) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="fin-card fin-promo fin-promo--green">
              <h3>预算执行健康度</h3>
              <p>基于各核心指标预算偏差率估算的综合吻合度（数值越高表示整体越接近预算）。</p>
              <p style="margin: 0; font-size: 22px; font-weight: 800; color: var(--color-ink)">
                {{ budgetHealthLabel }}
              </p>
            </div>

            <div class="fin-card fin-promo">
              <h3>最近异常指标</h3>
              <p>按预算偏差率绝对值排序，优先关注波动最大的指标。</p>
              <ul style="margin: 0; padding-left: 18px; color: var(--color-muted); font-size: 13px; line-height: 1.6">
                <li v-for="row in topAnomalies" :key="row.metricCode">
                  <strong style="color: var(--color-ink)">{{ row.metricName }}</strong>
                  ：偏差率 {{ formatPercent(row.varianceRate) }}
                </li>
                <li v-if="topAnomalies.length === 0">暂无偏差数据</li>
              </ul>
              <RouterLink class="fin-btn fin-btn--ghost" style="margin-top: 12px" to="/budget">查看预算偏差</RouterLink>
            </div>
          </section>
        </div>

        <aside class="fin-rail" aria-label="洞察与操作">
          <AiSummaryCard v-if="aiSummary" :summary="aiSummary" link-to="/ai" />
          <RiskAlertList :risks="risks" @select="onRiskSelect" />
          <div class="fin-card fin-promo fin-promo--green">
            <h3>预算偏差提醒</h3>
            <p>本期核心指标与预算对比已更新，可查看 AI 解释与明细表。</p>
            <RouterLink class="fin-btn fin-btn--primary" to="/budget">打开预算偏差</RouterLink>
          </div>
          <div class="fin-card fin-promo">
            <h3>快捷操作</h3>
            <p>一键进入常用分析流程。</p>
            <div style="display: flex; flex-direction: column; gap: 8px">
              <RouterLink class="fin-btn fin-btn--ghost" to="/ai">深度 AI 分析</RouterLink>
              <RouterLink class="fin-btn fin-btn--ghost" to="/reports">生成经营报告</RouterLink>
              <RouterLink class="fin-btn fin-btn--ghost" to="/risks">风险总览</RouterLink>
            </div>
          </div>
          <section v-if="aiErrorMessage" class="state-card state-card--warning fin-card" role="status">
            {{ aiErrorMessage }}
          </section>
        </aside>
      </div>
    </template>

    <div
      v-if="selectedRisk"
      class="fin-dialog-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="risk-dialog-title"
      @click.self="closeRiskDialog"
    >
      <div class="fin-dialog" @click.stop>
        <h2 id="risk-dialog-title">{{ selectedRisk.type }}</h2>
        <p>
          <strong>等级：</strong>{{ selectedRisk.level }}<br />
          {{ selectedRisk.message }}
        </p>
        <p style="margin-top: 12px">
          建议在「风险总览」与「预算偏差」中交叉查看相关指标，并结合 AI 分析形成处理优先级。
        </p>
        <div class="fin-dialog__actions">
          <button type="button" class="fin-btn fin-btn--ghost" @click="closeRiskDialog">关闭</button>
          <RouterLink class="fin-btn fin-btn--primary" to="/risks" @click="closeRiskDialog">风险总览</RouterLink>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 20px 0;
}

@media (max-width: 1024px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
