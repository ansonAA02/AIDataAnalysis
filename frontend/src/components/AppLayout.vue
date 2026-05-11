<script setup lang="ts">
import { 
  LayoutDashboard, 
  LineChart, 
  PieChart, 
  Sparkles, 
  FileText, 
  AlertTriangle, 
  Settings,
  HelpCircle
} from 'lucide-vue-next';
import AiAssistantFloating from './AiAssistantFloating.vue';
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar" aria-label="Main Navigation">
      <div class="brand">
        <div class="brand-mark">
          <LineChart :size="20" stroke-width="2.5" />
        </div>
        <span class="brand-text">AI Finance</span>
      </div>
      <nav class="nav-list">
        <RouterLink data-testid="nav-dashboard" to="/" class="nav-item">
          <LayoutDashboard :size="18" class="nav-icon" />
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink data-testid="nav-finance" to="/finance" class="nav-item">
          <PieChart :size="18" class="nav-icon" />
          <span>Finance Analysis</span>
        </RouterLink>
        <RouterLink data-testid="nav-budget" to="/budget" class="nav-item">
          <LineChart :size="18" class="nav-icon" />
          <span>Budget Variance</span>
        </RouterLink>
        <RouterLink data-testid="nav-ai" to="/ai" class="nav-item">
          <Sparkles :size="18" class="nav-icon" />
          <span>AI Insights</span>
        </RouterLink>
        <RouterLink data-testid="nav-reports" to="/reports" class="nav-item">
          <FileText :size="18" class="nav-icon" />
          <span>Reports</span>
        </RouterLink>
        <RouterLink data-testid="nav-risks" to="/risks" class="nav-item">
          <AlertTriangle :size="18" class="nav-icon" />
          <span>Risk Alerts</span>
        </RouterLink>
        <RouterLink data-testid="nav-notes" to="/notes" class="nav-item">
          <HelpCircle :size="18" class="nav-icon" />
          <span>Data Notes</span>
        </RouterLink>
      </nav>
      <nav class="nav-list nav-list--footer" aria-label="Secondary Navigation">
        <RouterLink data-testid="nav-settings" to="/settings" class="nav-item">
          <Settings :size="18" class="nav-icon" />
          <span>Settings</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="main-panel">
      <RouterView />
    </section>
    
    <AiAssistantFloating />
  </div>
</template>

<style scoped>
.nav-list--footer {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}

.sidebar {
  display: flex;
  flex-direction: column;
}

.brand {
  margin-bottom: 24px;
}

.brand-text {
  font-size: 18px;
  letter-spacing: -0.01em;
}

.brand-mark {
  background: var(--color-primary);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-icon {
  flex-shrink: 0;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.nav-item:hover .nav-icon,
.nav-item.router-link-active .nav-icon {
  opacity: 1;
}
</style>
