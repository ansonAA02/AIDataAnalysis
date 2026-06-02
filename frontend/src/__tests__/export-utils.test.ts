// Unit tests for the Excel/PDF export helpers. We mock XLSX so we never
// actually write a real .xlsx file during tests, and we mock window.print
// so we never block on a real print dialog.
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

// Hoisted mock holders so the vi.mock() factory (which itself runs hoisted)
// can reference them without TDZ errors.
const mocks = vi.hoisted(() => {
  return {
    writeFileMock: vi.fn(),
    jsonToSheetMock: vi.fn((rows: unknown) => ({ __rows: rows })),
    bookNewMock: vi.fn(() => ({ SheetNames: [], Sheets: {} })),
    bookAppendSheetMock: vi.fn(),
  };
});

// Mock the entire xlsx module so no real Excel logic runs in jsdom.
vi.mock('xlsx', () => ({
  utils: {
    json_to_sheet: mocks.jsonToSheetMock,
    book_new: mocks.bookNewMock,
    book_append_sheet: mocks.bookAppendSheetMock,
  },
  writeFile: mocks.writeFileMock,
}));

import {
  exportBudgetVariancesToExcel,
  exportDashboardToExcel,
  exportReportToExcel,
} from '../utils/exportExcel';
import { buildPrintFilename, printAsPdf } from '../utils/printPdf';
import type {
  BudgetVariance,
  BusinessLineMetric,
  DashboardOverview,
  Report,
  RiskAlert,
} from '../types/finance';

// Shared fixtures used across the export tests.
const sampleVariances: BudgetVariance[] = [
  {
    metricCode: 'REVENUE',
    metricName: '主营业务收入',
    actualValue: 1200,
    budgetValue: 1000,
    varianceAmount: 200,
    varianceRate: 20,
    unit: '万元',
  },
];

const sampleOverview: DashboardOverview = {
  periodId: 4,
  periodLabel: '2024-04',
  unit: '万元',
  revenue: 1200,
  cost: 600,
  grossProfit: 600,
  operatingExpense: 200,
  netProfit: 400,
  operatingCashFlow: 350,
  grossMargin: 50,
  netMargin: 33.3,
};

const sampleLines: BusinessLineMetric[] = [
  {
    businessLine: '零售',
    revenue: 800,
    cost: 400,
    grossProfit: 400,
    netProfit: 280,
    unit: '万元',
  },
];

const sampleRisks: RiskAlert[] = [
  { type: 'BUDGET_OVERSPEND', level: 'HIGH', message: '主营成本超支 12%' },
];

const sampleReport: Report = {
  periodId: 4,
  periodLabel: '2024-04',
  title: '2024-04 经营报告',
  unit: '万元',
  sections: [
    { code: 'OVERVIEW', title: '经营概览', content: '本月整体表现良好。' },
    { code: 'REVENUE', title: '收入分析', content: '收入同比增长 12%。' },
  ],
};

// Reset all xlsx spies between tests so call counts stay isolated.
beforeEach(() => {
  mocks.writeFileMock.mockClear();
  mocks.jsonToSheetMock.mockClear();
  mocks.bookNewMock.mockClear();
  mocks.bookAppendSheetMock.mockClear();
});

describe('exportExcel helpers', () => {
  it('exports budget variances with mapped Chinese headers and a single sheet', () => {
    exportBudgetVariancesToExcel(sampleVariances, '2024-04');

    expect(mocks.jsonToSheetMock).toHaveBeenCalledTimes(1);
    const passedRows = mocks.jsonToSheetMock.mock.calls[0][0] as Record<string, unknown>[];
    expect(passedRows[0]).toMatchObject({
      指标编码: 'REVENUE',
      指标名称: '主营业务收入',
      '偏差率(%)': 20,
    });

    expect(mocks.bookAppendSheetMock).toHaveBeenCalledTimes(1);
    expect(mocks.bookAppendSheetMock.mock.calls[0][2]).toBe('预算偏差');

    expect(mocks.writeFileMock).toHaveBeenCalledWith(
      expect.anything(),
      '预算偏差_2024-04.xlsx',
      { bookType: 'xlsx' },
    );
  });

  it('exports the monthly report into one sheet with section rows', () => {
    exportReportToExcel(sampleReport);

    expect(mocks.jsonToSheetMock).toHaveBeenCalledTimes(1);
    const rows = mocks.jsonToSheetMock.mock.calls[0][0] as Record<string, unknown>[];
    expect(rows).toHaveLength(2);
    expect(rows[0]).toMatchObject({ 序号: 1, 章节编码: 'OVERVIEW' });

    expect(mocks.writeFileMock).toHaveBeenCalledWith(
      expect.anything(),
      '经营报告_2024-04.xlsx',
      { bookType: 'xlsx' },
    );
  });

  it('exports the dashboard snapshot with KPI / 业务线 / 预算偏差 / 风险 sheets', () => {
    exportDashboardToExcel({
      overview: sampleOverview,
      businessLines: sampleLines,
      variances: sampleVariances,
      risks: sampleRisks,
    });

    // Four sheets appended in the documented order.
    expect(mocks.bookAppendSheetMock).toHaveBeenCalledTimes(4);
    const sheetNames = mocks.bookAppendSheetMock.mock.calls.map((call) => call[2]);
    expect(sheetNames).toEqual(['KPI', '业务线', '预算偏差', '风险']);

    expect(mocks.writeFileMock).toHaveBeenCalledWith(
      expect.anything(),
      '经营驾驶舱_2024-04.xlsx',
      { bookType: 'xlsx' },
    );
  });
});

describe('printPdf helpers', () => {
  // Restore document.title and window.print between tests.
  let originalTitle: string;
  let printSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    originalTitle = document.title;
    printSpy = vi.fn();
    Object.defineProperty(window, 'print', {
      configurable: true,
      writable: true,
      value: printSpy,
    });
    vi.useFakeTimers();
  });

  afterEach(() => {
    document.title = originalTitle;
    vi.useRealTimers();
  });

  it('builds a "context_period" filename hint', () => {
    expect(buildPrintFilename('经营报告', '2024-04')).toBe('经营报告_2024-04');
  });

  it('temporarily swaps document.title around window.print()', () => {
    document.title = 'AI Finance';
    printAsPdf('经营报告_2024-04');

    // While print() ran, the title was the filename hint.
    expect(printSpy).toHaveBeenCalledTimes(1);

    // The deferred restore happens via setTimeout(..., 0).
    vi.runAllTimers();
    expect(document.title).toBe('AI Finance');
  });
});
