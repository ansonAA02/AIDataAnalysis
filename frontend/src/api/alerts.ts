import http from './http';
import type { ApiResponse, AlertStatus, RiskAlertEvent } from '../types/finance';

// Recompute alerts for a period and persist any new ones.
export async function syncAlerts(periodId: number): Promise<RiskAlertEvent[]> {
  const response = await http.post<ApiResponse<RiskAlertEvent[]>>('/api/alerts/sync', null, {
    params: { periodId },
  });
  return response.data.data;
}

// List alerts filtered by readiness state ("unread" or "all").
export async function listAlerts(scope: 'unread' | 'all' = 'unread'): Promise<RiskAlertEvent[]> {
  const response = await http.get<ApiResponse<RiskAlertEvent[]>>('/api/alerts', {
    params: { scope },
  });
  return response.data.data;
}

// Fetch the unread count for the bell badge.
export async function getUnreadCount(): Promise<number> {
  const response = await http.get<ApiResponse<{ count: number }>>('/api/alerts/unread-count');
  return response.data.data.count;
}

// Update workflow status of a single alert event.
export async function updateAlertStatus(id: number, status: AlertStatus): Promise<RiskAlertEvent> {
  const response = await http.patch<ApiResponse<RiskAlertEvent>>(`/api/alerts/${id}/status`, {
    status,
  });
  return response.data.data;
}

// Mark every unread alert as ACKNOWLEDGED in one shot.
export async function markAllAlertsRead(): Promise<number> {
  const response = await http.post<ApiResponse<{ updated: number }>>('/api/alerts/mark-all-read');
  return response.data.data.updated;
}
