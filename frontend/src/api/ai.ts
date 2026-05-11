import http from './http';
import type { AiAnswer, AiAskRequest, ApiResponse } from '../types/finance';

export async function getAiSummary(periodId: number): Promise<AiAnswer> {
  const response = await http.get<ApiResponse<AiAnswer>>('/api/ai/summary', {
    params: { periodId },
  });
  return response.data.data;
}

export async function askAi(request: AiAskRequest): Promise<AiAnswer> {
  const response = await http.post<ApiResponse<AiAnswer>>('/api/ai/ask', request);
  return response.data.data;
}

export async function getAiSuggestions(periodId: number): Promise<string[]> {
  const response = await http.get<ApiResponse<string[]>>('/api/ai/suggestions', {
    params: { periodId },
  });
  return response.data.data;
}
