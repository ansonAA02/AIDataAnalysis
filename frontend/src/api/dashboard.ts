import http from './http';
import type { ApiResponse, DashboardOverview, RiskAlert, TrendPoint } from '../types/finance';

export async function getDashboardOverview(periodId: number): Promise<DashboardOverview> {
  const response = await http.get<ApiResponse<DashboardOverview>>('/api/dashboard/overview', {
    params: { periodId },
  });
  return response.data.data;
}

export async function getDashboardTrends(months = 12): Promise<TrendPoint[]> {
  const response = await http.get<ApiResponse<TrendPoint[]>>('/api/dashboard/trends', {
    params: { months },
  });
  return response.data.data;
}

export async function getDashboardRisks(periodId: number): Promise<RiskAlert[]> {
  const response = await http.get<ApiResponse<RiskAlert[]>>('/api/dashboard/risks', {
    params: { periodId },
  });
  return response.data.data;
}
