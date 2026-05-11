import { flushPromises, mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import ReportView from '../views/ReportView.vue';
import type { FinancePeriod, Report } from '../types/finance';

vi.mock('../api/finance', () => ({
  getPeriods: vi.fn(),
}));

vi.mock('../api/report', () => ({
  getMonthlyReport: vi.fn(),
}));

import { getPeriods } from '../api/finance';
import { getMonthlyReport } from '../api/report';

const periods: FinancePeriod[] = [
  { id: 4, yearValue: 2025, monthValue: 9, quarterValue: 3, periodLabel: '2025-09' },
  { id: 5, yearValue: 2025, monthValue: 10, quarterValue: 4, periodLabel: '2025-10' },
];

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
  it('renders report cover, period selector, and fixed report sections', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    vi.mocked(getMonthlyReport).mockResolvedValue(report);

    const wrapper = mount(ReportView);
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

  it('loads a new report when selected period changes', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    vi.mocked(getMonthlyReport).mockResolvedValue(report);
    const wrapper = mount(ReportView);
    await flushPromises();

    await wrapper.get('select').setValue('5');
    await flushPromises();

    expect(getMonthlyReport).toHaveBeenLastCalledWith(5);
  });

  it('renders loading state before report data resolves', () => {
    vi.mocked(getPeriods).mockReturnValue(new Promise(() => {}));
    vi.mocked(getMonthlyReport).mockReturnValue(new Promise(() => {}));

    const wrapper = mount(ReportView);

    expect(wrapper.text()).toContain('正在生成经营报告');
  });

  it('renders accessible error state when report API fails', async () => {
    vi.mocked(getPeriods).mockResolvedValue(periods);
    vi.mocked(getMonthlyReport).mockRejectedValue(new Error('network down'));

    const wrapper = mount(ReportView);
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('经营报告生成失败');
  });
});
