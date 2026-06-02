// Global alerts store: tracks unread count + bell-dropdown items so any view
// (AppLayout, RiskAlertsView) can mutate and observe risk alert workflow.
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import {
  getUnreadCount,
  listAlerts,
  markAllAlertsRead,
  syncAlerts,
  updateAlertStatus,
} from '../api/alerts';
import type { AlertStatus, RiskAlertEvent } from '../types/finance';

export const useAlertsStore = defineStore('alerts', () => {
  // All alert events for the management view (newest-first).
  const events = ref<RiskAlertEvent[]>([]);
  // Cached unread count for the bell badge (avoids re-deriving from events when none are loaded).
  const unreadCount = ref(0);
  // Loading flag exposed to the bell dropdown for a spinner.
  const loading = ref(false);
  // Last error surfaced to UI banners.
  const errorMessage = ref('');

  // Compute only the unread items for the bell dropdown.
  const unreadEvents = computed(() => events.value.filter((event) => event.status === 'NEW'));

  // Refresh just the unread count (cheap call used on a polling interval).
  async function refreshUnreadCount(): Promise<void> {
    try {
      unreadCount.value = await getUnreadCount();
    } catch (err) {
      // Swallow polling errors so the bell never crashes the app shell.
      errorMessage.value = '加载告警计数失败。';
      throw err;
    }
  }

  // Refresh the full event list (used by management view + dropdown open).
  async function refreshEvents(scope: 'unread' | 'all' = 'all'): Promise<void> {
    loading.value = true;
    errorMessage.value = '';
    try {
      events.value = await listAlerts(scope);
      if (scope === 'all') {
        unreadCount.value = events.value.filter((event) => event.status === 'NEW').length;
      } else {
        unreadCount.value = events.value.length;
      }
    } catch (err) {
      errorMessage.value = '加载告警事件失败。';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Recompute alerts from current period snapshot, then refresh local state.
  async function syncForPeriod(periodId: number): Promise<void> {
    loading.value = true;
    errorMessage.value = '';
    try {
      events.value = await syncAlerts(periodId);
      unreadCount.value = events.value.filter((event) => event.status === 'NEW').length;
    } catch (err) {
      errorMessage.value = '同步告警事件失败。';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Update one event's status and patch the local copy in place.
  async function changeStatus(id: number, status: AlertStatus): Promise<void> {
    const updated = await updateAlertStatus(id, status);
    const index = events.value.findIndex((event) => event.id === id);
    if (index >= 0) {
      events.value.splice(index, 1, updated);
    }
    unreadCount.value = events.value.filter((event) => event.status === 'NEW').length;
  }

  // Mark every unread alert as ACKNOWLEDGED then refresh.
  async function markAllRead(): Promise<void> {
    await markAllAlertsRead();
    await refreshEvents('all');
  }

  return {
    events,
    unreadCount,
    unreadEvents,
    loading,
    errorMessage,
    refreshUnreadCount,
    refreshEvents,
    syncForPeriod,
    changeStatus,
    markAllRead,
  };
});
