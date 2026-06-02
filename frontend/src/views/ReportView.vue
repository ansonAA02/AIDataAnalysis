<script setup lang="ts">
// Report view: subscribes to the global Pinia period store so the monthly
// report regenerates whenever the user switches period from the top bar.
// The local PeriodSelector has been removed.
import { computed, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { getMonthlyReport } from '../api/report';
import ReportSection from '../components/ReportSection.vue';
import { usePeriodStore } from '../stores/period';
import { exportReportToExcel } from '../utils/exportExcel';
import { buildPrintFilename, printAsPdf } from '../utils/printPdf';
import type { Report } from '../types/finance';

// Fallback period id for isolated test mounts.
const DEFAULT_PERIOD_ID = 4;

const periodStore = usePeriodStore();
const { currentPeriodId } = storeToRefs(periodStore);

const report = ref<Report | null>(null);
const loading = ref(true);
const errorMessage = ref('');

const executiveSummary = computed(() => report.value?.sections.find((section) => section.code === 'OVERVIEW'));
const reportProgress = computed(() => (report.value ? `${report.value.sections.length} 个章节` : '待生成'));

// Resolve the active period id with a safe fallback.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

// Fetch the monthly report for the active period.
async function loadReport() {
  const periodId = resolvePeriodId();
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

// Reload when the global period changes.
watch(currentPeriodId, (next, prev) => {
  if (next === prev) return;
  void loadReport();
});

// Export the current report to an .xlsx file (one sheet per section).
function handleExportExcel() {
  if (!report.value) return;
  exportReportToExcel(report.value);
}

// Trigger native browser print dialog so user can save the report as PDF.
function handleExportPdf() {
  if (!report.value) return;
  const filename = buildPrintFilename('经营报告', report.value.periodLabel ?? '');
  printAsPdf(filename);
}

onMounted(loadReport);
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
      <div class="report-unit">
        <span>报告单位</span>
        <strong>{{ report?.unit ?? '万元' }}</strong>
      </div>
      <div class="report-export export-actions">
        <button
          type="button"
          class="export-btn"
          :disabled="!report || loading"
          @click="handleExportExcel"
        >
          导出 Excel
        </button>
        <button
          type="button"
          class="export-btn export-btn--primary"
          :disabled="!report || loading"
          @click="handleExportPdf"
        >
          导出 PDF
        </button>
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

.report-export {
  display: flex;
  gap: 12px;
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

.export-btn--primary {
  color: #fff;
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.export-btn--primary:hover:not(:disabled) {
  background: var(--color-primary-dark, #1d4ed8);
  border-color: var(--color-primary-dark, #1d4ed8);
}
</style>
