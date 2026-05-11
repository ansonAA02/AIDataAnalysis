import http from './http';
import type { ApiResponse, Report } from '../types/finance';

export async function getMonthlyReport(periodId: number): Promise<Report> {
  const response = await http.get<ApiResponse<Report>>('/api/reports/monthly', {
    params: { periodId },
  });
  return response.data.data;
}
