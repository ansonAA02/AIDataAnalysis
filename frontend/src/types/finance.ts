export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export type MetricCode =
  | 'REVENUE'
  | 'COST'
  | 'GROSS_PROFIT'
  | 'OPERATING_EXPENSE'
  | 'NET_PROFIT'
  | 'OPERATING_CASH_FLOW';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export type RiskType =
  | 'CASH_FLOW_RISK'
  | 'PROFIT_MARGIN_DROP'
  | 'COST_GROWTH_FAST'
  | 'BUDGET_OVERSPEND'
  | 'REVENUE_DECLINE';

export interface FinancePeriod {
  id: number;
  yearValue: number;
  monthValue: number;
  quarterValue: number;
  periodLabel: string;
}

export interface FinanceMetric {
  metricCode: MetricCode;
  metricName: string;
  metricCategory: string;
  actualValue: number;
  unit: string;
}

export interface BusinessLineMetric {
  businessLine: string;
  revenue: number;
  cost: number;
  grossProfit: number;
  netProfit: number;
  unit: string;
}

export interface DashboardOverview {
  periodId: number;
  periodLabel: string;
  revenue: number;
  cost: number;
  grossProfit: number;
  operatingExpense: number;
  netProfit: number;
  operatingCashFlow: number;
  grossMargin: number;
  netMargin: number;
  unit: string;
}

export interface TrendPoint extends DashboardOverview {
}

export interface RiskAlert {
  type: RiskType;
  level: RiskLevel;
  message: string;
}

export interface AiAnswer {
  conclusion: string;
  evidence: string;
  analysis: string;
  risk: string;
  recommendation: string;
}

export interface AiAskRequest {
  periodId: number;
  question: string;
}

export interface BudgetVariance {
  metricCode: MetricCode;
  metricName: string;
  actualValue: number;
  budgetValue: number;
  varianceAmount: number;
  varianceRate: number;
  unit: string;
}

export interface BudgetExplanation {
  periodId: number;
  explanation: AiAnswer;
}

export interface ReportSection {
  code: string;
  title: string;
  content: string;
}

export interface Report {
  periodId: number;
  periodLabel: string;
  title: string;
  unit: string;
  sections: ReportSection[];
}
