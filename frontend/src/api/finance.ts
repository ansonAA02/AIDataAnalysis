import http from './http';
import type { ApiResponse, BusinessLineMetric, FinanceMetric, FinancePeriod } from '../types/finance';

export async function getPeriods(): Promise<FinancePeriod[]> {
  const response = await http.get<ApiResponse<FinancePeriod[]>>('/api/finance/periods');
  return response.data.data;
}

export async function getMetrics(periodId: number): Promise<FinanceMetric[]> {
  const response = await http.get<ApiResponse<FinanceMetric[]>>('/api/finance/metrics', {
    params: { periodId },
  });
  return response.data.data;
}

export async function getBusinessLines(periodId: number): Promise<BusinessLineMetric[]> {
  const response = await http.get<ApiResponse<BusinessLineMetric[]>>('/api/finance/business-lines', {
    params: { periodId },
  });
  return response.data.data;
}
