// Global period store: single source of truth for the currently selected
// finance reporting period. Persists the selection to localStorage so that
// switching between Dashboard / Budget / AI / Report views stays in sync.
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getPeriods } from '../api/finance';
import type { FinancePeriod } from '../types/finance';

const STORAGE_KEY = 'ai-finance:current-period-id';

// Read previously selected period id from browser storage.
function loadPersistedPeriodId(): number | null {
  if (typeof window === 'undefined') {
    return null;
  }
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

// Persist selected period id so it survives page reloads.
function savePersistedPeriodId(id: number | null): void {
  if (typeof window === 'undefined') {
    return;
  }
  if (id == null) {
    window.localStorage.removeItem(STORAGE_KEY);
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, String(id));
}

export const usePeriodStore = defineStore('period', () => {
  // Reactive list of all available reporting periods returned by the backend.
  const periods = ref<FinancePeriod[]>([]);
  // Currently selected period id, null until periods are loaded.
  const currentPeriodId = ref<number | null>(loadPersistedPeriodId());
  // Loading flag for the periods request, exposed to UI for spinners.
  const loading = ref(false);
  // Last error message from loading periods, surfaced to the layout banner.
  const errorMessage = ref('');

  // Convenience getter that returns the full FinancePeriod entity for the
  // currently selected id (or null when nothing is selected).
  const currentPeriod = computed(() => {
    if (currentPeriodId.value == null) {
      return null;
    }
    return periods.value.find((period) => period.id === currentPeriodId.value) ?? null;
  });

  // Load all periods from the backend once and pick a sensible default
  // (persisted -> last available -> first available).
  async function loadPeriods(): Promise<void> {
    if (loading.value) {
      return;
    }
    loading.value = true;
    errorMessage.value = '';
    try {
      const list = await getPeriods();
      periods.value = list;
      // If the persisted id is no longer valid (e.g. data reloaded), fall back.
      const persisted = currentPeriodId.value;
      const persistedIsValid = persisted != null && list.some((p) => p.id === persisted);
      if (!persistedIsValid) {
        const fallback = list.length ? list[list.length - 1].id : null;
        currentPeriodId.value = fallback;
        savePersistedPeriodId(fallback);
      }
    } catch (err) {
      errorMessage.value = '无法加载财务期间列表。';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Update the selected period id and persist it.
  function setCurrentPeriod(id: number | null): void {
    currentPeriodId.value = id;
    savePersistedPeriodId(id);
  }

  return {
    periods,
    currentPeriodId,
    currentPeriod,
    loading,
    errorMessage,
    loadPeriods,
    setCurrentPeriod,
  };
});
