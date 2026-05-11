import { beforeEach, describe, expect, it, vi } from 'vitest';

const httpMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
}));

vi.mock('../api/http', () => ({
  default: httpMock,
}));

import { askAi, getAiSuggestions, getAiSummary } from '../api/ai';
import { getBudgetExplanation, getBudgetVariances } from '../api/budget';
import { getDashboardOverview, getDashboardRisks, getDashboardTrends } from '../api/dashboard';
import { getBusinessLines, getMetrics, getPeriods } from '../api/finance';
import { getMonthlyReport } from '../api/report';

describe('frontend API clients', () => {
  beforeEach(() => {
    httpMock.get.mockResolvedValue({ data: { code: 'SUCCESS', message: 'ok', data: null } });
    httpMock.post.mockResolvedValue({ data: { code: 'SUCCESS', message: 'ok', data: null } });
    vi.clearAllMocks();
  });

  it('calls finance and dashboard backend paths', async () => {
    await getPeriods();
    await getMetrics(4);
    await getBusinessLines(4);
    await getDashboardOverview(4);
    await getDashboardTrends(12);
    await getDashboardRisks(4);

    expect(httpMock.get).toHaveBeenCalledWith('/api/finance/periods');
    expect(httpMock.get).toHaveBeenCalledWith('/api/finance/metrics', { params: { periodId: 4 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/finance/business-lines', { params: { periodId: 4 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/dashboard/overview', { params: { periodId: 4 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/dashboard/trends', { params: { months: 12 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/dashboard/risks', { params: { periodId: 4 } });
  });

  it('calls ai, budget, and report backend paths', async () => {
    await getAiSummary(4);
    await askAi({ periodId: 4, question: '为什么本月利润下降？' });
    await getAiSuggestions(4);
    await getBudgetVariances(4);
    await getBudgetExplanation(4);
    await getMonthlyReport(4);

    expect(httpMock.get).toHaveBeenCalledWith('/api/ai/summary', { params: { periodId: 4 } });
    expect(httpMock.post).toHaveBeenCalledWith('/api/ai/ask', { periodId: 4, question: '为什么本月利润下降？' });
    expect(httpMock.get).toHaveBeenCalledWith('/api/ai/suggestions', { params: { periodId: 4 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/budget/variances', { params: { periodId: 4 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/budget/explanation', { params: { periodId: 4 } });
    expect(httpMock.get).toHaveBeenCalledWith('/api/reports/monthly', { params: { periodId: 4 } });
  });
});
