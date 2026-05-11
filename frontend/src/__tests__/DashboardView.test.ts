import { flushPromises, mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import { createMemoryHistory, createRouter } from 'vue-router';
import DashboardView from '../views/DashboardView.vue';

vi.mock('../api/dashboard', () => ({
  getDashboardOverview: vi.fn(),
  getDashboardTrends: vi.fn(),
  getDashboardRisks: vi.fn(),
}));

vi.mock('../api/ai', () => ({
  getAiSummary: vi.fn(),
}));

vi.mock('../api/finance', () => ({
  getPeriods: vi.fn(),
  getBusinessLines: vi.fn(),
}));

vi.mock('../api/budget', () => ({
  getBudgetVariances: vi.fn(),
}));

vi.mock('../components/TrendChart.vue', () => ({
  default: {
    props: ['trends'],
    template: '<section data-testid="trend-chart">趋势点 {{ trends.length }}</section>',
  },
}));

import { getAiSummary } from '../api/ai';
import { getBudgetVariances } from '../api/budget';
import { getDashboardOverview, getDashboardRisks, getDashboardTrends } from '../api/dashboard';
import { getBusinessLines, getPeriods } from '../api/finance';
import type { RiskAlert } from '../types/finance';

const overview = {
  periodId: 4,
  periodLabel: '2025-09',
  revenue: 1310,
  cost: 910,
  grossProfit: 400,
  operatingExpense: 245,
  netProfit: 155,
  operatingCashFlow: 120,
  grossMargin: 30.53,
  netMargin: 11.83,
  unit: '万元',
};

const trends = [
  overview,
  { ...overview, periodId: 5, periodLabel: '2025-10', revenue: 1260, netProfit: 160 },
];

const risks: RiskAlert[] = [
  {
    type: 'BUDGET_OVERSPEND',
    level: 'HIGH',
    message: '成本实际值超过预算，需要复盘预算执行。',
  },
];

const summary = {
  conclusion: '利润下降主要由成本异常增长导致。',
  evidence: '成本从 735 万元升至 910 万元。',
  analysis: '成本上升压缩毛利空间。',
  risk: '现金流和利润率承压。',
  recommendation: '复盘供应商和项目交付成本。',
};

const periods = [
  { id: 3, yearValue: 2025, monthValue: 8, quarterValue: 3, periodLabel: '2025-08' },
  { id: 4, yearValue: 2025, monthValue: 9, quarterValue: 3, periodLabel: '2025-09' },
];

const businessLines = [
  { businessLine: '企业服务', revenue: 790, cost: 560, grossProfit: 230, netProfit: 88, unit: '万元' },
  { businessLine: '订阅业务', revenue: 520, cost: 350, grossProfit: 170, netProfit: 67, unit: '万元' },
];

const variances = [
  {
    metricCode: 'COST' as const,
    metricName: '成本',
    actualValue: 910,
    budgetValue: 780,
    varianceAmount: 130,
    varianceRate: 16.67,
    unit: '万元',
  },
];

function mountDashboard() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: DashboardView },
      { path: '/finance', component: { template: '<div />' } },
      { path: '/budget', component: { template: '<div />' } },
      { path: '/ai', component: { template: '<div />' } },
      { path: '/reports', component: { template: '<div />' } },
      { path: '/risks', component: { template: '<div />' } },
    ],
  });

  return mount(DashboardView, {
    global: {
      plugins: [router],
    },
  });
}

describe('DashboardView', () => {
  it('renders loading state before dashboard data resolves', () => {
    vi.mocked(getPeriods).mockReturnValue(new Promise(() => {}));

    const wrapper = mountDashboard();

    expect(wrapper.text()).toContain('正在加载经营数据');
  });

  it('renders KPI, trend, risk, and AI summary sections from backend data', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    vi.mocked(getDashboardOverview).mockResolvedValue(overview);
    vi.mocked(getDashboardTrends).mockResolvedValue(trends);
    vi.mocked(getDashboardRisks).mockResolvedValue(risks);
    vi.mocked(getBusinessLines).mockResolvedValue(businessLines);
    vi.mocked(getBudgetVariances).mockResolvedValue(variances);
    vi.mocked(getAiSummary).mockResolvedValue(summary);

    const wrapper = mountDashboard();
    await flushPromises();

    expect(wrapper.text()).toContain('本月经营概览');
    expect(wrapper.text()).toContain('2025-09 核心指标');
    expect(wrapper.text()).toContain('收入');
    expect(wrapper.text()).toContain('1,310.00 万元');
    expect(wrapper.text()).toContain('毛利');
    expect(wrapper.text()).toContain('经营现金流');
    expect(wrapper.get('[data-testid="trend-chart"]').text()).toContain('趋势点 2');
    expect(wrapper.text()).toContain('BUDGET_OVERSPEND');
    expect(wrapper.text()).toContain('利润下降主要由成本异常增长导致。');
    expect(wrapper.text()).toContain('企业服务');
  });

  it('keeps dashboard data visible when AI summary API fails', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    vi.mocked(getDashboardOverview).mockResolvedValue(overview);
    vi.mocked(getDashboardTrends).mockResolvedValue(trends);
    vi.mocked(getDashboardRisks).mockResolvedValue(risks);
    vi.mocked(getBusinessLines).mockResolvedValue(businessLines);
    vi.mocked(getBudgetVariances).mockResolvedValue(variances);
    vi.mocked(getAiSummary).mockRejectedValue(new Error('missing api key'));

    const wrapper = mountDashboard();
    await flushPromises();

    expect(wrapper.text()).toContain('2025-09 核心指标');
    expect(wrapper.text()).toContain('1,310.00 万元');
    expect(wrapper.text()).toContain('AI 经营摘要暂不可用');
  });

  it('renders an accessible error state when dashboard APIs fail', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    vi.mocked(getDashboardOverview).mockRejectedValue(new Error('network down'));
    vi.mocked(getDashboardTrends).mockResolvedValue([]);
    vi.mocked(getDashboardRisks).mockResolvedValue([]);
    vi.mocked(getBusinessLines).mockResolvedValue([]);
    vi.mocked(getBudgetVariances).mockResolvedValue([]);
    vi.mocked(getAiSummary).mockResolvedValue(summary);

    const wrapper = mountDashboard();
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('经营数据加载失败');
  });
});
