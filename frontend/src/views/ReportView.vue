<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { getPeriods } from '../api/finance';
import { getMonthlyReport } from '../api/report';
import PeriodSelector from '../components/PeriodSelector.vue';
import ReportSection from '../components/ReportSection.vue';
import type { FinancePeriod, Report } from '../types/finance';

const periods = ref<FinancePeriod[]>([]);
const selectedPeriodId = ref<number | null>(null);
const report = ref<Report | null>(null);
const loading = ref(true);
const errorMessage = ref('');

const executiveSummary = computed(() => report.value?.sections.find((section) => section.code === 'OVERVIEW'));
const reportProgress = computed(() => (report.value ? `${report.value.sections.length} 个章节` : '待生成'));

async function loadInitialReport() {
  loading.value = true;
  errorMessage.value = '';

  try {
    periods.value = await getPeriods();
    selectedPeriodId.value = periods.value[0]?.id ?? 4;
    await loadReport(selectedPeriodId.value);
  } catch {
    errorMessage.value = '经营报告生成失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
}

async function loadReport(periodId: number | null) {
  if (periodId == null) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    report.value = await getMonthlyReport(periodId);
  } catch {
    errorMessage.value = '经营报告生成失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
}

watch(selectedPeriodId, (periodId, previousPeriodId) => {
  if (previousPeriodId != null && periodId !== previousPeriodId) {
    void loadReport(periodId);
  }
});

onMounted(loadInitialReport);
</script>

<template>
  <main class="report-page" aria-labelledby="report-title">
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow">Board Intelligence Brief</p>
        <h2 id="report-title">董事会经营报告</h2>
        <p>将经营概览、收入、成本、利润、现金流、预算、风险和建议沉淀为可阅读的结构化报告。</p>
      </div>
      <div class="report-meta">
        <span>{{ report?.periodLabel ?? '选择期间' }}</span>
        <strong>{{ reportProgress }}</strong>
      </div>
    </section>

    <section class="report-toolbar panel-card" style="margin-bottom: 24px;">
      <PeriodSelector v-model="selectedPeriodId" :periods="periods" />
      <div class="report-unit">
        <span>报告单位</span>
        <strong>{{ report?.unit ?? '万元' }}</strong>
      </div>
    </section>

    <section v-if="loading" class="state-card report-loading" aria-live="polite">正在生成经营报告...</section>
    <section v-else-if="errorMessage" class="state-card state-card--error" role="alert">
      {{ errorMessage }}
    </section>

    <template v-else-if="report">
      <section class="report-title-card">
        <p class="eyebrow">Monthly Report</p>
        <h2>{{ report.title }}</h2>
        <p>{{ executiveSummary?.content }}</p>
      </section>

      <section class="report-section-flow" aria-label="经营报告章节">
        <ReportSection
          v-for="(section, index) in report.sections"
          :key="section.code"
          :section="section"
          :index="index"
        />
      </section>
    </template>
  </main>
</template>

<style scoped>
.report-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: var(--shadow-card);
}

.report-meta strong {
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
}

.report-meta span {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 4px;
}

.report-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
}

.report-unit {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.report-unit span {
  color: var(--color-muted);
}

.report-unit strong {
  color: var(--color-ink);
  font-weight: 600;
}

.report-title-card {
  padding: 32px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: var(--shadow-card);
  margin-bottom: 24px;
}

.report-title-card h2 {
  font-family: "Lyon Text", "Playfair Display", serif;
  font-size: 1.8rem;
  margin: 8px 0 16px;
  color: var(--color-ink);
}

.report-title-card p {
  font-size: 15px;
  line-height: 1.6;
  color: var(--color-muted);
}

.report-section-flow {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
