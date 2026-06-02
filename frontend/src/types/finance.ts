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

// Workflow status of a persisted risk alert event.
export type AlertStatus = 'NEW' | 'ACKNOWLEDGED' | 'RESOLVED' | 'IGNORED';

// Persistent risk alert event row returned from /api/alerts.
export interface RiskAlertEvent {
  id: number;
  periodId: number;
  alertType: RiskType;
  alertLevel: RiskLevel;
  message: string;
  status: AlertStatus;
  createdAt: string;
  resolvedAt: string | null;
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

// Single message inside a stored AI conversation history record.
export interface AiHistoryMessage {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  createdAt: string;
}

// Lightweight conversation summary used by the history list.
export interface AiConversationSummary {
  id: number;
  title: string;
  periodId: number | null;
  favorited: boolean;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
  preview: string;
}

// Full conversation detail returned when a session is opened.
export interface AiConversationDetail extends AiConversationSummary {
  messages: AiHistoryMessage[];
}

// Payload used to append a Q&A turn to a (possibly new) conversation.
export interface AppendTurnPayload {
  conversationId: number | null;
  periodId: number;
  question: string;
  answer: string;
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
