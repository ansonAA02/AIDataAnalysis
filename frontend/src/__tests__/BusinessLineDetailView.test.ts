// Test BusinessLineDetailView: verifies it picks the line by route param,
// computes shares/margins correctly, and offers peer ranking links to other lines.
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createMemoryHistory } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import BusinessLineDetailView from '../views/BusinessLineDetailView.vue';
import type { BudgetVariance, BusinessLineMetric, DashboardOverview } from '../types/finance';

// Mock the three APIs used by this view.
vi.mock('../api/budget', () => ({ getBudgetVariances: vi.fn() }));
vi.mock('../api/dashboard', () => ({ getDashboardOverview: vi.fn() }));
vi.mock('../api/finance', () => ({ getBusinessLines: vi.fn() }));

import { getBudgetVariances } from '../api/budget';
import { getDashboardOverview } from '../api/dashboard';
import { getBusinessLines } from '../api/finance';

// Sample fixtures for assertions below.
const sampleLines: BusinessLineMetric[] = [
  { businessLine: '零售', revenue: 800, cost: 400, grossProfit: 400, netProfit: 280, unit: '万元' },
  { businessLine: '批发', revenue: 500, cost: 350, grossProfit: 150, netProfit: 90, unit: '万元' },
  { businessLine: '电商', revenue: 200, cost: 110, grossProfit: 90, netProfit: 50, unit: '万元' },
];

const sampleOverview: DashboardOverview = {
  periodId: 4,
  periodLabel: '2024-04',
  unit: '万元',
  revenue: 1500,
  cost: 860,
  grossProfit: 640,
  operatingExpense: 200,
  netProfit: 420,
  operatingCashFlow: 350,
  grossMargin: 42.7,
  netMargin: 28,
};

const sampleVariances: BudgetVariance[] = [];

// Build a router with the dynamic detail route + a stub dashboard route,
// then push the URL synchronously before mounting the component.
async function mountDetail(lineName: string) {
  const pinia = createPinia();
  setActivePinia(pinia);
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'dashboard', component: { template: '<div />' } },
      { path: '/business-lines/:lineName', name: 'business-line-detail', component: BusinessLineDetailView },
    ],
  });
  await router.push(`/business-lines/${encodeURIComponent(lineName)}`);
  await router.isReady();
  return mount(BusinessLineDetailView, {
    global: { plugins: [pinia, router] },
  });
}

describe('BusinessLineDetailView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(getBusinessLines).mockResolvedValue(sampleLines);
    vi.mocked(getDashboardOverview).mockResolvedValue(sampleOverview);
    vi.mocked(getBudgetVariances).mockResolvedValue(sampleVariances);
  });

  it('renders the selected line with KPI, shares, margins, and ranking', async () => {
    const wrapper = await mountDetail('零售');
    await flushPromises();

    // Backend APIs called with the fallback period id (4).
    expect(getBusinessLines).toHaveBeenCalledWith(4);
    expect(getDashboardOverview).toHaveBeenCalledWith(4);
    expect(getBudgetVariances).toHaveBeenCalledWith(4);

    const text = wrapper.text();
    // Title shows the line name
    expect(text).toContain('零售');
    // KPI numbers rendered
    expect(text).toContain('800.00 万元');
    expect(text).toContain('280.00 万元');
    // Revenue share = 800 / 1500 = 53.3%
    expect(text).toContain('53.3%');
    // Profit share = 280 / 420 = 66.7%
    expect(text).toContain('66.7%');
    // Gross margin = 400 / 800 = 50.0%
    expect(text).toContain('50.0%');
    // Net margin = 280 / 800 = 35.0%
    expect(text).toContain('35.0%');
    // Peer table includes other lines
    expect(text).toContain('批发');
    expect(text).toContain('电商');
  });

  it('shows a not-found state when the URL line name does not exist', async () => {
    const wrapper = await mountDetail('不存在的线');
    await flushPromises();

    expect(wrapper.text()).toContain('未找到');
  });

  it('ranks the current line by revenue (零售 should be #1)', async () => {
    const wrapper = await mountDetail('零售');
    await flushPromises();

    expect(wrapper.text()).toContain('第 1 名');
  });
});
