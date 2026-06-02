import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { usePeriodStore } from '../stores/period';

vi.mock('../api/finance', () => ({
  getPeriods: vi.fn(),
}));

import { getPeriods } from '../api/finance';

const periods = [
  { id: 3, yearValue: 2025, monthValue: 8, quarterValue: 3, periodLabel: '2025-08' },
  { id: 4, yearValue: 2025, monthValue: 9, quarterValue: 3, periodLabel: '2025-09' },
];

describe('usePeriodStore', () => {
  beforeEach(() => {
    // Reset Pinia, mocks and persisted storage before each test for isolation.
    setActivePinia(createPinia());
    vi.clearAllMocks();
    window.localStorage.clear();
  });

  it('starts with no period selected and empty list', () => {
    const store = usePeriodStore();
    expect(store.currentPeriodId).toBeNull();
    expect(store.periods).toEqual([]);
    expect(store.currentPeriod).toBeNull();
  });

  it('loads periods and defaults to the last one when nothing is persisted', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    const store = usePeriodStore();

    await store.loadPeriods();

    expect(store.periods).toEqual(periods);
    expect(store.currentPeriodId).toBe(4);
    expect(store.currentPeriod?.periodLabel).toBe('2025-09');
  });

  it('keeps a valid persisted period id after loading periods', async () => {
    window.localStorage.setItem('ai-finance:current-period-id', '3');
    vi.mocked(getPeriods).mockResolvedValue(periods);
    const store = usePeriodStore();

    await store.loadPeriods();

    expect(store.currentPeriodId).toBe(3);
  });

  it('setCurrentPeriod updates state and persists the selection', () => {
    const store = usePeriodStore();
    store.setCurrentPeriod(7);

    expect(store.currentPeriodId).toBe(7);
    expect(window.localStorage.getItem('ai-finance:current-period-id')).toBe('7');
  });

  it('exposes an error message when the periods API fails', async () => {
    vi.mocked(getPeriods).mockRejectedValue(new Error('network down'));
    const store = usePeriodStore();

    await expect(store.loadPeriods()).rejects.toThrow('network down');
    expect(store.errorMessage).toContain('无法加载');
  });
});
