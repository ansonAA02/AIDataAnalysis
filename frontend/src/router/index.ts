import { createRouter, createWebHistory } from 'vue-router';
import AiAnalysisView from '../views/AiAnalysisView.vue';
import BudgetVarianceView from '../views/BudgetVarianceView.vue';
import DashboardView from '../views/DashboardView.vue';
import DataNotesView from '../views/DataNotesView.vue';
import FinanceAnalysisView from '../views/FinanceAnalysisView.vue';
import HomeView from '../views/HomeView.vue';
import ReportView from '../views/ReportView.vue';
import RiskAlertsView from '../views/RiskAlertsView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/welcome',
      name: 'welcome',
      component: HomeView,
    },
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/ai',
      name: 'ai-analysis',
      component: AiAnalysisView,
    },
    {
      path: '/budget',
      name: 'budget-variance',
      component: BudgetVarianceView,
    },
    {
      path: '/reports',
      name: 'reports',
      component: ReportView,
    },
    {
      path: '/finance',
      name: 'finance-analysis',
      component: FinanceAnalysisView,
    },
    {
      path: '/risks',
      name: 'risk-alerts',
      component: RiskAlertsView,
    },
    {
      path: '/notes',
      name: 'data-notes',
      component: DataNotesView,
    },
  ],
});

export default router;
