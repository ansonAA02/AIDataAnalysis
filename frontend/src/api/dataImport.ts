import http from './http';
import type { ApiResponse } from '../types/finance';

// Single parsed CSV row returned by the preview endpoint.
export interface ImportPreviewRow {
  rowNumber: number;
  periodId: number | null;
  periodLabel: string | null;
  metricCode: string | null;
  metricName: string | null;
  metricCategory: string | null;
  actualValue: number | null;
  unit: string | null;
  valid: boolean;
  error: string | null;
}

// Aggregate response for the CSV preview endpoint.
export interface ImportPreviewResponse {
  totalRows: number;
  validRows: number;
  invalidRows: number;
  headers: string[];
  rows: ImportPreviewRow[];
}

// Aggregate response for the CSV commit endpoint.
export interface ImportCommitResponse {
  processedRows: number;
  insertedRows: number;
  updatedRows: number;
  skippedRows: number;
}

// Upload a CSV and get back validation results without persisting.
export async function previewFinanceMetrics(file: File): Promise<ImportPreviewResponse> {
  const form = new FormData();
  form.append('file', file);
  const response = await http.post<ApiResponse<ImportPreviewResponse>>(
    '/api/import/finance-metrics/preview',
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return response.data.data;
}

// Re-upload the same CSV to commit valid rows into the database.
export async function commitFinanceMetrics(file: File): Promise<ImportCommitResponse> {
  const form = new FormData();
  form.append('file', file);
  const response = await http.post<ApiResponse<ImportCommitResponse>>(
    '/api/import/finance-metrics/commit',
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return response.data.data;
}
