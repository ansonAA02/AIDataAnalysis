// Excel export helpers built on top of SheetJS (xlsx). Each helper takes
// already-fetched view data and produces a downloadable .xlsx file in the
// user's browser. Backend untouched.
import * as XLSX from 'xlsx';
import type {
  BudgetVariance,
  BusinessLineMetric,
  DashboardOverview,
  Report,
  RiskAlert,
} from '../types/finance';

// Trigger a browser download of a workbook with the given filename.
function downloadWorkbook(workbook: XLSX.WorkBook, filename: string): void {
  XLSX.writeFile(workbook, filename, { bookType: 'xlsx' });
}

// Append a sheet built from an array of row objects with friendly headers.
function appendSheet<T extends Record<string, unknown>>(
  workbook: XLSX.WorkBook,
  sheetName: string,
  rows: T[],
): void {
  const worksheet = XLSX.utils.json_to_sheet(rows);
  // Sheet names cannot exceed 31 chars in the .xlsx spec.
  const safeName = sheetName.slice(0, 31);
  XLSX.utils.book_append_sheet(workbook, worksheet, safeName);
}

// Export the budget variance table to a single-sheet workbook.
export function exportBudgetVariancesToExcel(
  variances: BudgetVariance[],
  periodLabel: string,
): void {
  const rows = variances.map((variance) => ({
    指标编码: variance.metricCode,
    指标名称: variance.metricName,
    实际值: variance.actualValue,
    预算值: variance.budgetValue,
    偏差金额: variance.varianceAmount,
    '偏差率(%)': variance.varianceRate,
    单位: variance.unit,
  }));
  const workbook = XLSX.utils.book_new();
  appendSheet(workbook, '预算偏差', rows);
  downloadWorkbook(workbook, `预算偏差_${periodLabel}.xlsx`);
}

// Export the monthly board report to a multi-section workbook (one row per
// section so it stays readable when opened in Excel).
export function exportReportToExcel(report: Report): void {
  const rows = report.sections.map((section, index) => ({
    序号: index + 1,
    章节编码: section.code,
    章节名称: section.title,
    内容: section.content,
  }));
  const workbook = XLSX.utils.book_new();
  appendSheet(workbook, report.periodLabel || '月度报告', rows);
  downloadWorkbook(workbook, `经营报告_${report.periodLabel}.xlsx`);
}

// Export the dashboard snapshot to a multi-sheet workbook covering KPI,
// business lines, budget variance and risk alerts.
export function exportDashboardToExcel(input: {
  overview: DashboardOverview;
  businessLines: BusinessLineMetric[];
  variances: BudgetVariance[];
  risks: RiskAlert[];
}): void {
  const { overview, businessLines, variances, risks } = input;

  const kpiRows = [
    { 指标: '收入', 数值: overview.revenue, 单位: overview.unit },
    { 指标: '成本', 数值: overview.cost, 单位: overview.unit },
    { 指标: '毛利', 数值: overview.grossProfit, 单位: overview.unit },
    { 指标: '营业费用', 数值: overview.operatingExpense, 单位: overview.unit },
    { 指标: '净利润', 数值: overview.netProfit, 单位: overview.unit },
    { 指标: '经营现金流', 数值: overview.operatingCashFlow, 单位: overview.unit },
    { 指标: '毛利率(%)', 数值: overview.grossMargin, 单位: '%' },
    { 指标: '净利率(%)', 数值: overview.netMargin, 单位: '%' },
  ];

  const lineRows = businessLines.map((line) => ({
    业务线: line.businessLine,
    收入: line.revenue,
    成本: line.cost,
    毛利: line.grossProfit,
    净利润: line.netProfit,
    单位: line.unit,
  }));

  const varianceRows = variances.map((variance) => ({
    指标编码: variance.metricCode,
    指标名称: variance.metricName,
    实际值: variance.actualValue,
    预算值: variance.budgetValue,
    偏差金额: variance.varianceAmount,
    '偏差率(%)': variance.varianceRate,
    单位: variance.unit,
  }));

  const riskRows = risks.map((risk) => ({
    类型: risk.type,
    级别: risk.level,
    描述: risk.message,
  }));

  const workbook = XLSX.utils.book_new();
  appendSheet(workbook, 'KPI', kpiRows);
  appendSheet(workbook, '业务线', lineRows);
  appendSheet(workbook, '预算偏差', varianceRows);
  appendSheet(workbook, '风险', riskRows);
  downloadWorkbook(workbook, `经营驾驶舱_${overview.periodLabel}.xlsx`);
}
