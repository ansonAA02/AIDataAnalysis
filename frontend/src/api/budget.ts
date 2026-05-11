import http from './http';
import type { ApiResponse, BudgetExplanation, BudgetVariance } from '../types/finance';

export async function getBudgetVariances(periodId: number): Promise<BudgetVariance[]> {
  const response = await http.get<ApiResponse<BudgetVariance[]>>('/api/budget/variances', {
    params: { periodId },
  });
  return response.data.data;
}

export async function getBudgetExplanation(periodId: number): Promise<BudgetExplanation> {
  const response = await http.get<ApiResponse<BudgetExplanation>>('/api/budget/explanation', {
    params: { periodId },
  });
  return response.data.data;
}
