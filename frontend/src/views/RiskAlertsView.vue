<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { getDashboardRisks } from '../api/dashboard';
import { getPeriods } from '../api/finance';
import PeriodSelector from '../components/PeriodSelector.vue';
import RiskAlertList from '../components/RiskAlertList.vue';
import type { FinancePeriod, RiskAlert } from '../types/finance';

const periods = ref<FinancePeriod[]>([]);
const selectedPeriodId = ref<number | null>(null);
const risks = ref<RiskAlert[]>([]);
const loading = ref(true);
const selected = ref<RiskAlert | null>(null);

async function load() {
  const id = selectedPeriodId.value;
  if (id == null) {
    return;
  }
  loading.value = true;
  try {
    risks.value = await getDashboardRisks(id);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  periods.value = await getPeriods();
  const list = periods.value;
  selectedPeriodId.value = (list.length ? list[list.length - 1] : undefined)?.id ?? 4;
  await load();
});

watch(selectedPeriodId, (id, prev) => {
  if (id != null && prev != null && id !== prev) {
    void load();
  }
});
</script>

<template>
  <main class="risk-page page-surface">
    <header class="page-hero">
      <div>
        <p class="eyebrow">Risk Alerts</p>
        <h1>风险总览</h1>
        <p class="muted-text">按期间查看系统识别的风险信号，点击条目可阅读说明。</p>
      </div>
      <PeriodSelector
        v-if="periods.length && selectedPeriodId != null"
        :periods="periods"
        :model-value="selectedPeriodId"
        @update:model-value="(id) => (selectedPeriodId = id)"
      />
    </header>

    <section v-if="loading" class="state-card">加载中…</section>
    <RiskAlertList v-else :risks="risks" @select="(r) => (selected = r)" />

    <div v-if="selected" class="fin-dialog-backdrop" role="dialog" aria-modal="true" @click.self="selected = null">
      <div class="fin-dialog" @click.stop>
        <h2>{{ selected.type }}</h2>
        <p><strong>等级：</strong>{{ selected.level }}</p>
        <p>{{ selected.message }}</p>
        <div class="fin-dialog__actions">
          <button type="button" class="fin-btn fin-btn--ghost" @click="selected = null">关闭</button>
        </div>
      </div>
    </div>
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
  margin-bottom: 16px;
}

.page-hero h1 {
  margin: 6px 0 8px;
}
</style>
