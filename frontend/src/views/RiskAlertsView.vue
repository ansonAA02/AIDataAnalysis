<script setup lang="ts">
// Risk alerts view: subscribes to the global Pinia period store + alerts store.
// Now supports event lifecycle (resolve / ignore / acknowledge) on top of
// the legacy read-only "current period" risk list.
import { computed, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { Check, X, Eye, RefreshCw } from 'lucide-vue-next';
import { useAlertsStore } from '../stores/alerts';
import { usePeriodStore } from '../stores/period';
import type { AlertStatus, RiskAlertEvent } from '../types/finance';

// Fallback period id used only for isolated test mounts.
const DEFAULT_PERIOD_ID = 4;

const periodStore = usePeriodStore();
const alertsStore = useAlertsStore();
const { currentPeriodId } = storeToRefs(periodStore);
const { events, loading, errorMessage } = storeToRefs(alertsStore);

// Filter chip selection: which lifecycle bucket to display.
type Filter = 'ALL' | 'NEW' | 'ACKNOWLEDGED' | 'RESOLVED' | 'IGNORED';
const activeFilter = ref<Filter>('ALL');

// Selected event for detail dialog.
const selected = ref<RiskAlertEvent | null>(null);

// Resolve the active period id with a safe fallback for tests.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

// Pull events filtered by the active chip.
const filteredEvents = computed<RiskAlertEvent[]>(() => {
  if (activeFilter.value === 'ALL') {
    return events.value;
  }
  return events.value.filter((event) => event.status === activeFilter.value);
});

// Counts shown beside each filter chip.
const counts = computed(() => ({
  ALL: events.value.length,
  NEW: events.value.filter((e) => e.status === 'NEW').length,
  ACKNOWLEDGED: events.value.filter((e) => e.status === 'ACKNOWLEDGED').length,
  RESOLVED: events.value.filter((e) => e.status === 'RESOLVED').length,
  IGNORED: events.value.filter((e) => e.status === 'IGNORED').length,
}));

// Pretty label for each filter chip (Simplified Chinese).
function filterLabel(filter: Filter): string {
  const map: Record<Filter, string> = {
    ALL: '全部',
    NEW: '未读',
    ACKNOWLEDGED: '已读',
    RESOLVED: '已处理',
    IGNORED: '已忽略',
  };
  return map[filter];
}

// Pretty label for an alert level.
function levelLabel(level: string): string {
  return ({ LOW: '低', MEDIUM: '中', HIGH: '高' } as Record<string, string>)[level] ?? level;
}

// Pretty label for a workflow status.
function statusLabel(status: AlertStatus): string {
  return (
    {
      NEW: '未读',
      ACKNOWLEDGED: '已读',
      RESOLVED: '已处理',
      IGNORED: '已忽略',
    } as Record<AlertStatus, string>
  )[status];
}

// Re-sync alerts for the active period from server snapshot.
async function syncFromPeriod() {
  try {
    await alertsStore.syncForPeriod(resolvePeriodId());
  } catch {
    /* error already surfaced via store */
  }
}

// Update one event's status with optimistic UI via the store action.
async function changeStatus(id: number, status: AlertStatus) {
  try {
    await alertsStore.changeStatus(id, status);
  } catch {
    /* error already surfaced via store */
  }
}

// Re-sync whenever the global period changes.
watch(currentPeriodId, (next, prev) => {
  if (next === prev) {
    return;
  }
  void syncFromPeriod();
});

onMounted(syncFromPeriod);
</script>

<template>
  <main class="risk-page page-surface">
    <header class="page-hero">
      <div>
        <p class="eyebrow">Risk Alerts</p>
        <h1>风险告警管理</h1>
        <p class="muted-text">查看、处理或忽略系统检测到的风险信号。</p>
      </div>
      <button
        type="button"
        class="fin-btn fin-btn--ghost"
        data-testid="risks-refresh"
        :disabled="loading"
        @click="syncFromPeriod"
      >
        <RefreshCw :size="14" :class="{ spin: loading }" />
        重新同步
      </button>
    </header>

    <p v-if="errorMessage" class="state-card state-card--warning" role="status">
      {{ errorMessage }}
    </p>

    <nav class="filter-row" aria-label="告警筛选">
      <button
        v-for="key in ['ALL', 'NEW', 'ACKNOWLEDGED', 'RESOLVED', 'IGNORED'] as Filter[]"
        :key="key"
        type="button"
        class="filter-chip"
        :class="{ active: activeFilter === key }"
        :data-testid="`risks-filter-${key.toLowerCase()}`"
        @click="activeFilter = key"
      >
        {{ filterLabel(key) }}
        <span class="chip-count">{{ counts[key] }}</span>
      </button>
    </nav>

    <section v-if="loading && events.length === 0" class="state-card">加载中…</section>
    <section
      v-else-if="filteredEvents.length === 0"
      class="state-card"
      data-testid="risks-empty-state"
    >
      暂无符合条件的告警事件 🎉
    </section>
    <ul v-else class="event-list" data-testid="risks-event-list">
      <li
        v-for="event in filteredEvents"
        :key="event.id"
        class="event-row"
        :class="`level-${event.alertLevel.toLowerCase()}`"
      >
        <div class="event-main">
          <div class="event-meta">
            <span class="level-tag">{{ levelLabel(event.alertLevel) }}</span>
            <span class="status-tag" :data-status="event.status">{{ statusLabel(event.status) }}</span>
            <span class="muted-text type-tag">{{ event.alertType }}</span>
          </div>
          <p class="event-message">{{ event.message }}</p>
        </div>
        <div class="event-actions">
          <button
            type="button"
            class="fin-btn fin-btn--ghost"
            data-testid="risks-view"
            @click="selected = event"
          >
            <Eye :size="14" /> 查看
          </button>
          <button
            v-if="event.status === 'NEW' || event.status === 'ACKNOWLEDGED'"
            type="button"
            class="fin-btn fin-btn--primary"
            data-testid="risks-resolve"
            @click="changeStatus(event.id, 'RESOLVED')"
          >
            <Check :size="14" /> 处理
          </button>
          <button
            v-if="event.status !== 'IGNORED'"
            type="button"
            class="fin-btn fin-btn--ghost"
            data-testid="risks-ignore"
            @click="changeStatus(event.id, 'IGNORED')"
          >
            <X :size="14" /> 忽略
          </button>
        </div>
      </li>
    </ul>

    <div
      v-if="selected"
      class="fin-dialog-backdrop"
      role="dialog"
      aria-modal="true"
      @click.self="selected = null"
    >
      <div class="fin-dialog" @click.stop>
        <h2>{{ selected.alertType }}</h2>
        <p><strong>等级：</strong>{{ levelLabel(selected.alertLevel) }}</p>
        <p><strong>状态：</strong>{{ statusLabel(selected.status) }}</p>
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
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.page-hero h1 {
  margin: 6px 0 8px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.filter-chip:hover {
  border-color: var(--color-primary);
}

.filter-chip.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.chip-count {
  font-size: 11px;
  padding: 0 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999px;
}

.filter-chip:not(.active) .chip-count {
  background: var(--color-border);
  color: var(--color-muted);
}

.event-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.event-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 10px;
}

.event-main {
  flex: 1;
  min-width: 0;
}

.event-meta {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 4px;
}

.level-tag,
.status-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.level-tag {
  background: rgba(220, 38, 38, 0.12);
  color: #b91c1c;
}

.event-row.level-medium .level-tag {
  background: rgba(217, 119, 6, 0.12);
  color: #b45309;
}

.event-row.level-low .level-tag {
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
}

.status-tag {
  background: var(--color-border);
  color: var(--color-muted);
}

.status-tag[data-status='NEW'] {
  background: rgba(220, 38, 38, 0.12);
  color: #b91c1c;
}

.status-tag[data-status='RESOLVED'] {
  background: rgba(22, 163, 74, 0.12);
  color: #15803d;
}

.type-tag {
  font-size: 12px;
  font-family: 'SFMono-Regular', Consolas, monospace;
}

.event-message {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: var(--color-text);
  word-break: break-word;
}

.event-actions {
  display: inline-flex;
  flex-shrink: 0;
  gap: 6px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
