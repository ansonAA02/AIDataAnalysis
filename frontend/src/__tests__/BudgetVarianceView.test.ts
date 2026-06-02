import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import BudgetVarianceView from '../views/BudgetVarianceView.vue';
import type { BudgetExplanation, BudgetVariance } from '../types/finance';

vi.mock('../api/budget', () => ({
  getBudgetExplanation: vi.fn(),
  getBudgetVariances: vi.fn(),
}));

import { getBudgetExplanation, getBudgetVariances } from '../api/budget';

// Mount the budget view with a fresh Pinia instance so the period store is
// isolated between tests. The view falls back to DEFAULT_PERIOD_ID = 4.
function mountBudget() {
  const pinia = createPinia();
  setActivePinia(pinia);
  return mount(BudgetVarianceView, {
    global: { plugins: [pinia] },
  });
}

const variances: BudgetVariance[] = [
  {
    metricCode: 'COST',
    metricName: '成本',
    actualValue: 910,
    budgetValue: 780,
    varianceAmount: 130,
    varianceRate: 16.67,
    unit: '万元',
  },
  {
    metricCode: 'REVENUE',
    metricName: '收入',
    actualValue: 1310,
    budgetValue: 1340,
    varianceAmount: -30,
    varianceRate: -2.24,
    unit: '万元',
  },
];

const explanation: BudgetExplanation = {
  periodId: 4,
  explanation: {
    conclusion: '成本预算超支是本期主要偏差。',
    evidence: '成本实际 910 万元，预算 780 万元。',
    analysis: '供应商和交付成本上涨。',
    risk: '利润率和现金流承压。',
    recommendation: '复盘供应商成本并收紧费用审批。',
  },
};

describe('BudgetVarianceView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders budget variance table and AI explanation', async () => {
    vi.mocked(getBudgetVariances).mockResolvedValue(variances);
    vi.mocked(getBudgetExplanation).mockResolvedValue(explanation);

    const wrapper = mountBudget();
    await flushPromises();

    expect(getBudgetVariances).toHaveBeenCalledWith(4);
    expect(getBudgetExplanation).toHaveBeenCalledWith(4);
    expect(wrapper.text()).toContain('预算偏差雷达');
    expect(wrapper.text()).toContain('成本');
    expect(wrapper.text()).toContain('910.00 万元');
    expect(wrapper.text()).toContain('780.00 万元');
    expect(wrapper.text()).toContain('16.67%');
    expect(wrapper.text()).toContain('超支');
    expect(wrapper.text()).toContain('成本预算超支是本期主要偏差。');
  });

  it('renders empty state when there is no variance data', async () => {
    vi.mocked(getBudgetVariances).mockResolvedValue([]);
    vi.mocked(getBudgetExplanation).mockResolvedValue(explanation);

    const wrapper = mountBudget();
    await flushPromises();

    expect(wrapper.text()).toContain('暂无预算偏差数据');
  });

  it('renders loading state before budget data resolves', () => {
    vi.mocked(getBudgetVariances).mockReturnValue(new Promise(() => {}));
    vi.mocked(getBudgetExplanation).mockReturnValue(new Promise(() => {}));

    const wrapper = mountBudget();

    expect(wrapper.text()).toContain('正在加载预算偏差');
  });

  it('renders accessible error state when variance API fails', async () => {
    vi.mocked(getBudgetVariances).mockRejectedValue(new Error('network down'));
    vi.mocked(getBudgetExplanation).mockResolvedValue(explanation);

    const wrapper = mountBudget();
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('预算偏差表加载失败');
  });

  it('still renders variance table when only explanation API fails', async () => {
    vi.mocked(getBudgetVariances).mockResolvedValue(variances);
    vi.mocked(getBudgetExplanation).mockRejectedValue(new Error('ai down'));

    const wrapper = mountBudget();
    await flushPromises();

    expect(wrapper.get('[role="status"]').text()).toContain('AI 偏差解释暂不可用');
    expect(wrapper.text()).toContain('910.00 万元');
    expect(wrapper.text()).not.toContain('成本预算超支是本期主要偏差。');
  });
});
