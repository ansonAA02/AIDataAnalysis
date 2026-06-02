<script setup lang="ts">
// AppLayout: shell with sidebar nav + a top bar that hosts the global
// period selector wired to the Pinia store. Switching the period here
// propagates instantly to every view via the shared store.
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import {
  LayoutDashboard,
  LineChart,
  PieChart,
  Sparkles,
  FileText,
  AlertTriangle,
  Settings,
  HelpCircle,
  Bell,
  Sun,
  Moon,
  Upload,
} from 'lucide-vue-next';
import AiAssistantFloating from './AiAssistantFloating.vue';
import PeriodSelector from './PeriodSelector.vue';
import AlertBellDropdown from './AlertBellDropdown.vue';
import { usePeriodStore } from '../stores/period';
import { useAlertsStore } from '../stores/alerts';
import { usePreferencesStore } from '../stores/preferences';

const periodStore = usePeriodStore();
const alertsStore = useAlertsStore();
const preferencesStore = usePreferencesStore();
// Use storeToRefs so template bindings stay reactive when destructured.
const { periods, currentPeriodId, errorMessage } = storeToRefs(periodStore);
const { unreadCount } = storeToRefs(alertsStore);
const { effectiveTheme } = storeToRefs(preferencesStore);

// Bell dropdown open state (toggle on icon click).
const isBellOpen = ref(false);
// Polling handle so we can cancel on unmount.
let pollHandle: ReturnType<typeof setInterval> | null = null;

// Forward the v-model update from PeriodSelector into the store action.
function onPeriodChange(id: number) {
  periodStore.setCurrentPeriod(id);
}

// Toggle the bell dropdown; lazy-load the latest alerts on first open.
async function toggleBell() {
  isBellOpen.value = !isBellOpen.value;
  if (isBellOpen.value) {
    try {
      await alertsStore.refreshEvents('all');
    } catch {
      // Error already surfaced through alertsStore.errorMessage.
    }
  }
}

// Quick-toggle theme between light and dark from the top bar.
function toggleTheme() {
  preferencesStore.setTheme(effectiveTheme.value === 'dark' ? 'light' : 'dark');
}

// Close handler used by the dropdown's outside-click + close button.
function closeBell() {
  isBellOpen.value = false;
}

// Re-sync alerts whenever the active period changes (so badge stays accurate).
watch(currentPeriodId, async (next) => {
  if (next == null) {
    return;
  }
  try {
    await alertsStore.syncForPeriod(next);
  } catch {
    // Error already exposed by the store.
  }
});

// Load periods exactly once when the layout first mounts. Views can rely
// on the store being populated by the time their own onMounted runs.
onMounted(async () => {
  if (periods.value.length === 0) {
    try {
      await periodStore.loadPeriods();
    } catch {
      // Error is already exposed via errorMessage; nothing else to do here.
    }
  }
  // Initial sync against current period so the bell badge reflects reality.
  if (currentPeriodId.value != null) {
    try {
      await alertsStore.syncForPeriod(currentPeriodId.value);
    } catch {
      // Ignore: store error is rendered separately if needed.
    }
  }
  // Lightweight 60s polling to keep the unread count fresh between actions.
  pollHandle = setInterval(() => {
    alertsStore.refreshUnreadCount().catch(() => {
      /* swallow polling errors so the bell stays mounted */
    });
  }, 60_000);
});

// Stop the polling timer when the layout is torn down.
onBeforeUnmount(() => {
  if (pollHandle) {
    clearInterval(pollHandle);
    pollHandle = null;
  }
});
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar" aria-label="Main Navigation">
      <div class="brand">
        <div class="brand-mark">
          <LineChart :size="20" stroke-width="2.5" />
        </div>
        <span class="brand-text">AI Finance</span>
      </div>
      <nav class="nav-list">
        <RouterLink data-testid="nav-dashboard" to="/" class="nav-item">
          <LayoutDashboard :size="18" class="nav-icon" />
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink data-testid="nav-finance" to="/finance" class="nav-item">
          <PieChart :size="18" class="nav-icon" />
          <span>Finance Analysis</span>
        </RouterLink>
        <RouterLink data-testid="nav-budget" to="/budget" class="nav-item">
          <LineChart :size="18" class="nav-icon" />
          <span>Budget Variance</span>
        </RouterLink>
        <RouterLink data-testid="nav-ai" to="/ai" class="nav-item">
          <Sparkles :size="18" class="nav-icon" />
          <span>AI Insights</span>
        </RouterLink>
        <RouterLink data-testid="nav-reports" to="/reports" class="nav-item">
          <FileText :size="18" class="nav-icon" />
          <span>Reports</span>
        </RouterLink>
        <RouterLink data-testid="nav-risks" to="/risks" class="nav-item">
          <AlertTriangle :size="18" class="nav-icon" />
          <span>Risk Alerts</span>
        </RouterLink>
        <RouterLink data-testid="nav-notes" to="/notes" class="nav-item">
          <HelpCircle :size="18" class="nav-icon" />
          <span>Data Notes</span>
        </RouterLink>
        <RouterLink data-testid="nav-import" to="/import" class="nav-item">
          <Upload :size="18" class="nav-icon" />
          <span>Data Import</span>
        </RouterLink>
      </nav>
      <nav class="nav-list nav-list--footer" aria-label="Secondary Navigation">
        <RouterLink data-testid="nav-settings" to="/settings" class="nav-item">
          <Settings :size="18" class="nav-icon" />
          <span>Settings</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="main-panel">
      <!-- Global top bar so the period selector is always visible. -->
      <header class="top-bar" data-testid="global-top-bar">
        <div class="top-bar-left">
          <span class="top-bar-eyebrow">Reporting period</span>
        </div>
        <div class="top-bar-right">
          <button
            type="button"
            class="bell-button"
            data-testid="global-theme-toggle"
            :aria-label="effectiveTheme === 'dark' ? '切换到浅色' : '切换到深色'"
            @click="toggleTheme"
          >
            <Moon v-if="effectiveTheme === 'light'" :size="18" />
            <Sun v-else :size="18" />
          </button>
          <button
            data-testid="global-alert-bell"
            class="bell-button"
            type="button"
            :aria-label="`告警通知，未读 ${unreadCount} 条`"
            :aria-expanded="isBellOpen"
            @click="toggleBell"
          >
            <Bell :size="18" />
            <span
              v-if="unreadCount > 0"
              class="bell-badge"
              data-testid="global-alert-bell-badge"
            >{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </button>
          <PeriodSelector
            data-testid="global-period-selector"
            :periods="periods"
            :model-value="currentPeriodId"
            @update:model-value="onPeriodChange"
          />
        </div>
      </header>
      <AlertBellDropdown
        v-if="isBellOpen"
        data-testid="global-alert-dropdown"
        @close="closeBell"
      />
      <p
        v-if="errorMessage"
        class="state-card state-card--warning top-bar-error"
        role="status"
      >
        {{ errorMessage }}
      </p>
      <RouterView />
    </section>
    
    <AiAssistantFloating />
  </div>
</template>

<style scoped>
.nav-list--footer {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}

.sidebar {
  display: flex;
  flex-direction: column;
}

.brand {
  margin-bottom: 24px;
}

.brand-text {
  font-size: 18px;
  letter-spacing: -0.01em;
}

.brand-mark {
  background: var(--color-primary);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-icon {
  flex-shrink: 0;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.nav-item:hover .nav-icon,
.nav-item.router-link-active .nav-icon {
  opacity: 1;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.top-bar-eyebrow {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-muted);
  font-weight: 600;
}

.top-bar-error {
  margin: 8px 24px 0;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bell-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.bell-button:hover {
  background: var(--color-surface-strong, rgba(0, 0, 0, 0.04));
  border-color: var(--color-primary);
}

.bell-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #dc2626;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 18px;
  text-align: center;
}
</style>
