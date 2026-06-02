<script setup lang="ts">
// AlertBellDropdown: floating list of risk alert events with quick
// per-row actions (resolve / ignore) plus a "mark all read" footer.
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { Check, X, AlertTriangle, RefreshCw, CheckCheck } from 'lucide-vue-next';
import { useAlertsStore } from '../stores/alerts';
import type { AlertStatus, RiskAlertEvent } from '../types/finance';

// Emits used by parent layout to close the dropdown.
const emit = defineEmits<{ (event: 'close'): void }>();

const alertsStore = useAlertsStore();
const { events, loading } = storeToRefs(alertsStore);

// Root element used to detect outside clicks.
const rootRef = ref<HTMLElement | null>(null);

// Show only the most recent 8 in the dropdown to keep it compact.
const visibleEvents = computed<RiskAlertEvent[]>(() => events.value.slice(0, 8));

// Translate raw alert level into a user-facing label.
function levelLabel(level: string): string {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高' };
  return map[level] ?? level;
}

// Translate raw alert status into a user-facing label.
function statusLabel(status: AlertStatus): string {
  const map: Record<AlertStatus, string> = {
    NEW: '未读',
    ACKNOWLEDGED: '已读',
    RESOLVED: '已处理',
    IGNORED: '已忽略',
  };
  return map[status];
}

// Update a single alert's status with optimistic UI via store action.
async function changeStatus(id: number, status: AlertStatus) {
  try {
    await alertsStore.changeStatus(id, status);
  } catch {
    /* error already surfaced via store.errorMessage */
  }
}

// Mark all unread items as ACKNOWLEDGED in one round-trip.
async function markAllRead() {
  try {
    await alertsStore.markAllRead();
  } catch {
    /* error already surfaced via store.errorMessage */
  }
}

// Outside-click handler that closes the dropdown when user clicks elsewhere.
function onDocumentClick(event: MouseEvent) {
  const target = event.target as Node | null;
  if (!rootRef.value || !target) {
    return;
  }
  // Ignore clicks on the bell button itself (toggle handled by parent).
  const bell = document.querySelector('[data-testid="global-alert-bell"]');
  if (bell && bell.contains(target)) {
    return;
  }
  if (!rootRef.value.contains(target)) {
    emit('close');
  }
}

onMounted(() => {
  document.addEventListener('mousedown', onDocumentClick);
});
onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onDocumentClick);
});
</script>

<template>
  <div ref="rootRef" class="alert-dropdown" role="dialog" aria-label="风险告警通知">
    <header class="dropdown-header">
      <span class="dropdown-title">
        <AlertTriangle :size="16" />
        风险告警
      </span>
      <button
        type="button"
        class="dropdown-action"
        data-testid="alert-mark-all-read"
        :disabled="loading"
        @click="markAllRead"
      >
        <CheckCheck :size="14" />
        全部标记已读
      </button>
    </header>

    <div v-if="loading" class="dropdown-empty">
      <RefreshCw :size="16" class="spin" />
      <span>加载中…</span>
    </div>
    <div
      v-else-if="visibleEvents.length === 0"
      class="dropdown-empty"
      data-testid="alert-empty-state"
    >
      <span>当前无风险告警 🎉</span>
    </div>
    <ul v-else class="dropdown-list">
      <li
        v-for="event in visibleEvents"
        :key="event.id"
        class="dropdown-item"
        :class="`level-${event.alertLevel.toLowerCase()}`"
        data-testid="alert-row"
      >
        <div class="item-main">
          <div class="item-meta">
            <span class="level-tag">{{ levelLabel(event.alertLevel) }}</span>
            <span class="status-tag" :data-status="event.status">{{ statusLabel(event.status) }}</span>
          </div>
          <p class="item-message">{{ event.message }}</p>
        </div>
        <div class="item-actions">
          <button
            v-if="event.status === 'NEW' || event.status === 'ACKNOWLEDGED'"
            type="button"
            class="action-button action-resolve"
            data-testid="alert-resolve"
            :aria-label="`标记处理 ${event.message}`"
            @click="changeStatus(event.id, 'RESOLVED')"
          >
            <Check :size="14" />
          </button>
          <button
            v-if="event.status !== 'IGNORED'"
            type="button"
            class="action-button action-ignore"
            data-testid="alert-ignore"
            :aria-label="`忽略 ${event.message}`"
            @click="changeStatus(event.id, 'IGNORED')"
          >
            <X :size="14" />
          </button>
        </div>
      </li>
    </ul>

    <footer class="dropdown-footer">
      <RouterLink to="/risks" class="dropdown-more" @click="emit('close')">
        查看全部告警 →
      </RouterLink>
    </footer>
  </div>
</template>

<style scoped>
.alert-dropdown {
  position: absolute;
  top: 64px;
  right: 24px;
  width: 380px;
  max-height: 480px;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  z-index: 50;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.dropdown-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
}

.dropdown-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
}

.dropdown-action:hover:not(:disabled) {
  background: rgba(37, 99, 235, 0.08);
}

.dropdown-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dropdown-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 16px;
  color: var(--color-muted);
  font-size: 13px;
}

.dropdown-list {
  list-style: none;
  margin: 0;
  padding: 0;
  overflow-y: auto;
  flex: 1;
}

.dropdown-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.dropdown-item:last-child {
  border-bottom: none;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.level-tag,
.status-tag {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.level-tag {
  background: rgba(220, 38, 38, 0.12);
  color: #b91c1c;
}

.level-medium .level-tag {
  background: rgba(217, 119, 6, 0.12);
  color: #b45309;
}

.level-low .level-tag {
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

.item-message {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-ink);
  word-break: break-word;
}

.item-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.action-resolve:hover {
  background: rgba(22, 163, 74, 0.1);
  color: #15803d;
  border-color: #15803d;
}

.action-ignore:hover {
  background: rgba(220, 38, 38, 0.1);
  color: #b91c1c;
  border-color: #b91c1c;
}

.dropdown-footer {
  padding: 10px 16px;
  text-align: center;
  border-top: 1px solid var(--color-border);
}

.dropdown-more {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
}

.dropdown-more:hover {
  text-decoration: underline;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
