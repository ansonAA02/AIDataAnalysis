<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router';
import { RouterLink } from 'vue-router';
import * as icons from 'lucide-vue-next';

const props = defineProps<{
  label: string;
  value: string;
  meta: string;
  tone?: 'primary' | 'warning' | 'success';
  to?: RouteLocationRaw;
  icon?: string;
}>();
</script>

<template>
  <component
    :is="to ? RouterLink : 'article'"
    :to="to"
    class="kpi-card"
    :class="[tone ? `kpi-card--${tone}` : '', to ? 'kpi-card--link' : '']"
  >
    <div class="kpi-card__header">
      <p class="kpi-card__label">{{ label }}</p>
      <component v-if="icon && icons[icon]" :is="icons[icon]" class="kpi-card__icon" :size="18" />
    </div>
    <strong class="kpi-card__value">{{ value }}</strong>
    <span class="kpi-card__meta">{{ meta }}</span>
  </component>
</template>

<style scoped>
.kpi-card {
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card--link:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
  border-color: var(--color-primary);
}

.kpi-card--primary { border-top: 3px solid var(--color-primary); }
.kpi-card--warning { border-top: 3px solid var(--color-warning); }
.kpi-card--success { border-top: 3px solid var(--color-success); }

.kpi-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.kpi-card__label {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-muted);
}

.kpi-card__icon {
  color: var(--color-muted);
  opacity: 0.5;
}

.kpi-card__value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-ink);
  font-family: "Geist Mono", "SF Mono", monospace;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.kpi-card__meta {
  font-size: 12px;
  color: var(--color-muted);
}

.kpi-card--primary .kpi-card__icon { color: var(--color-primary); opacity: 0.8; }
.kpi-card--warning .kpi-card__icon { color: var(--color-warning); opacity: 0.8; }
.kpi-card--success .kpi-card__icon { color: var(--color-success); opacity: 0.8; }
</style>
