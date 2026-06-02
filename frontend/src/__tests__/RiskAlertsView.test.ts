import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { createRouter, createMemoryHistory } from 'vue-router';
import RiskAlertsView from '../views/RiskAlertsView.vue';
import type { RiskAlertEvent } from '../types/finance';

// Spy mocks reused across cases so each test can assert call arguments.
const syncAlertsMock = vi.fn();
const updateAlertStatusMock = vi.fn();

// Mock the alerts API surface to avoid real HTTP calls.
vi.mock('../api/alerts', () => ({
  syncAlerts: (...args: unknown[]) => syncAlertsMock(...args),
  listAlerts: vi.fn().mockResolvedValue([]),
  getUnreadCount: vi.fn().mockResolvedValue(0),
  updateAlertStatus: (...args: unknown[]) => updateAlertStatusMock(...args),
  markAllAlertsRead: vi.fn().mockResolvedValue(0),
}));

// Build a small RiskAlertEvent fixture used by the management view.
function buildEvent(overrides: Partial<RiskAlertEvent> = {}): RiskAlertEvent {
  return {
    id: 1,
    periodId: 4,
    alertType: 'CASH_FLOW_RISK',
    alertLevel: 'HIGH',
    message: '现金流持续为负',
    status: 'NEW',
    createdAt: '2026-05-30T00:00:00Z',
    resolvedAt: null,
    ...overrides,
  };
}

beforeEach(() => {
  syncAlertsMock.mockReset();
  updateAlertStatusMock.mockReset();
});

describe('RiskAlertsView (lifecycle management)', () => {
  it('renders alerts returned from sync and supports the Resolve action', async () => {
    // Initial sync returns one NEW alert.
    syncAlertsMock.mockResolvedValueOnce([buildEvent()]);
    // Resolve action returns the same alert with status RESOLVED.
    updateAlertStatusMock.mockResolvedValueOnce(
      buildEvent({ status: 'RESOLVED', resolvedAt: '2026-05-30T01:00:00Z' }),
    );

    const pinia = createPinia();
    setActivePinia(pinia);
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }],
    });
    router.push('/');
    await router.isReady();

    const wrapper = mount(RiskAlertsView, {
      global: { plugins: [pinia, router] },
    });

    await flushPromises();

    // Row appears with the NEW status tag.
    const row = wrapper.get('[data-testid="risks-event-list"]');
    expect(row.text()).toContain('现金流持续为负');
    expect(row.html()).toContain('data-status="NEW"');

    // Click "处理" to mark the alert as RESOLVED.
    await wrapper.get('[data-testid="risks-resolve"]').trigger('click');
    await flushPromises();

    expect(updateAlertStatusMock).toHaveBeenCalledWith(1, 'RESOLVED');
    expect(wrapper.html()).toContain('data-status="RESOLVED"');
  });

  it('shows the empty state when no events are returned', async () => {
    syncAlertsMock.mockResolvedValueOnce([]);

    const pinia = createPinia();
    setActivePinia(pinia);
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }],
    });
    router.push('/');
    await router.isReady();

    const wrapper = mount(RiskAlertsView, {
      global: { plugins: [pinia, router] },
    });

    await flushPromises();
    expect(wrapper.get('[data-testid="risks-empty-state"]').text()).toContain('暂无');
  });
});
