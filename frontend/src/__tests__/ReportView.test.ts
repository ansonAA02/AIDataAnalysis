import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ReportView from '../views/ReportView.vue';
import { usePeriodStore } from '../stores/period';
import type { Report } from '../types/finance';

vi.mock('../api/report', () => ({
  getMonthlyReport: vi.fn(),
}));

import { getMonthlyReport } from '../api/report';

// Mount the report view with a fresh Pinia instance per test. The view
// falls back to DEFAULT_PERIOD_ID = 4 when the store has no period.
function mountReport() {
  const pinia = createPinia();
  setActivePinia(pinia);
  const wrapper = mount(ReportView, {
    global: { plugins: [pinia] },
  });
  return { wrapper, pinia };
}

const report: Report = {
  periodId: 4,
  periodLabel: '2025-09',
  title: '2025-09 月度经营分析报告',
  unit: '万元',
  sections: [
    { code: 'OVERVIEW', title: '经营概览', content: '本期利润承压。' },
    { code: 'REVENUE', title: '收入', content: '收入 1310.00 万元。' },
    { code: 'COST', title: '成本', content: '成本 910.00 万元。' },
    { code: 'PROFIT', title: '利润', content: '净利 155.00 万元。' },
    { code: 'CASH_FLOW', title: '现金流', content: '经营现金流 120.00 万元。' },
    { code: 'BUDGET', title: '预算', content: 'COST 偏差率 16.67%。' },
    { code: 'RISK', title: '风险', content: 'BUDGET_OVERSPEND HIGH。' },
    { code: 'SUGGESTION', title: '建议', content: '复盘供应商成本。' },
  ],
};

describe('ReportView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders report cover and fixed report sections', async () => {
    vi.mocked(getMonthlyReport).mockResolvedValue(report);

    const { wrapper } = mountReport();
    await flushPromises();

    expect(getMonthlyReport).toHaveBeenCalledWith(4);
    expect(wrapper.text()).toContain('董事会经营报告');
    expect(wrapper.text()).toContain('2025-09 月度经营分析报告');
    expect(wrapper.text()).toContain('经营概览');
    expect(wrapper.text()).toContain('收入');
    expect(wrapper.text()).toContain('成本');
    expect(wrapper.text()).toContain('利润');
    expect(wrapper.text()).toContain('现金流');
    expect(wrapper.text()).toContain('预算');
    expect(wrapper.text()).toContain('风险');
    expect(wrapper.text()).toContain('建议');
    expect(wrapper.text()).toContain('本期利润承压。');
  });

  it('reloads the report when the global period store changes', async () => {
    vi.mocked(getMonthlyReport).mockResolvedValue(report);
    const { wrapper } = mountReport();
    await flushPromises();

    // Switch the global period; the report view should refetch.
    const store = usePeriodStore();
    store.setCurrentPeriod(5);
    await flushPromises();

    expect(getMonthlyReport).toHaveBeenLastCalledWith(5);
    expect(wrapper.text()).toContain('董事会经营报告');
  });

  it('renders loading state before report data resolves', () => {
    vi.mocked(getMonthlyReport).mockReturnValue(new Promise(() => {}));

    const { wrapper } = mountReport();

    expect(wrapper.text()).toContain('正在生成经营报告');
  });

  it('renders accessible error state when report API fails', async () => {
    vi.mocked(getMonthlyReport).mockRejectedValue(new Error('network down'));

    const { wrapper } = mountReport();
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('经营报告生成失败');
  });
});
