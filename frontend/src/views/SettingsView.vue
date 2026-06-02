<script setup lang="ts">
// Settings view: surfaces the global preferences store + default period
// selection. All mutations persist immediately via the store's watcher.
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { Sun, Moon, Monitor, Languages, Layout, RotateCcw } from 'lucide-vue-next';
import { usePreferencesStore, type Density, type Locale, type ThemeMode } from '../stores/preferences';
import { usePeriodStore } from '../stores/period';

const preferencesStore = usePreferencesStore();
const periodStore = usePeriodStore();
const { theme, density, locale, defaultPeriodId, effectiveTheme } = storeToRefs(preferencesStore);
const { periods } = storeToRefs(periodStore);

// Static option lists used to render radio-style segmented controls.
const themeOptions: Array<{ value: ThemeMode; label: string; icon: typeof Sun }> = [
  { value: 'light', label: '浅色', icon: Sun },
  { value: 'dark', label: '深色', icon: Moon },
  { value: 'auto', label: '跟随系统', icon: Monitor },
];

const densityOptions: Array<{ value: Density; label: string }> = [
  { value: 'comfortable', label: '舒适' },
  { value: 'compact', label: '紧凑' },
];

const localeOptions: Array<{ value: Locale; label: string }> = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'zh-TW', label: '繁體中文' },
  { value: 'en', label: 'English' },
];

// Display the resolved effective theme so users understand "auto".
const effectiveThemeLabel = computed(() => (effectiveTheme.value === 'dark' ? '深色' : '浅色'));

// Apply default period: persist + immediately switch the active period.
function onDefaultPeriodChange(event: Event) {
  const value = (event.target as HTMLSelectElement).value;
  const parsed = value === '' ? null : Number(value);
  preferencesStore.setDefaultPeriodId(parsed);
  if (parsed != null) {
    periodStore.setCurrentPeriod(parsed);
  }
}

// Reset all preferences to their initial defaults.
function onReset() {
  preferencesStore.reset();
}
</script>

<template>
  <main class="settings-page page-surface" aria-labelledby="settings-title">
    <header class="page-hero">
      <div>
        <p class="eyebrow">System</p>
        <h1 id="settings-title">个人偏好</h1>
        <p class="muted-text">主题、语言、默认期间会自动保存到本地浏览器。</p>
      </div>
      <button
        type="button"
        class="fin-btn fin-btn--ghost"
        data-testid="settings-reset"
        @click="onReset"
      >
        <RotateCcw :size="14" /> 恢复默认
      </button>
    </header>

    <section class="setting-card" aria-labelledby="setting-theme-title">
      <header class="setting-header">
        <h2 id="setting-theme-title">主题外观</h2>
        <p class="muted-text">当前生效：{{ effectiveThemeLabel }}</p>
      </header>
      <div class="segmented" role="radiogroup" aria-label="主题">
        <button
          v-for="option in themeOptions"
          :key="option.value"
          type="button"
          role="radio"
          :aria-checked="theme === option.value"
          class="segmented-btn"
          :class="{ active: theme === option.value }"
          :data-testid="`settings-theme-${option.value}`"
          @click="preferencesStore.setTheme(option.value)"
        >
          <component :is="option.icon" :size="16" />
          {{ option.label }}
        </button>
      </div>
    </section>

    <section class="setting-card" aria-labelledby="setting-density-title">
      <header class="setting-header">
        <h2 id="setting-density-title">界面密度</h2>
        <p class="muted-text">紧凑模式下间距更小，适合大屏。</p>
      </header>
      <div class="segmented" role="radiogroup" aria-label="界面密度">
        <button
          v-for="option in densityOptions"
          :key="option.value"
          type="button"
          role="radio"
          :aria-checked="density === option.value"
          class="segmented-btn"
          :class="{ active: density === option.value }"
          :data-testid="`settings-density-${option.value}`"
          @click="preferencesStore.setDensity(option.value)"
        >
          <Layout :size="16" />
          {{ option.label }}
        </button>
      </div>
    </section>

    <section class="setting-card" aria-labelledby="setting-locale-title">
      <header class="setting-header">
        <h2 id="setting-locale-title">语言偏好</h2>
        <p class="muted-text">用于 AI 回复以及未来的国际化文案。</p>
      </header>
      <div class="segmented" role="radiogroup" aria-label="语言">
        <button
          v-for="option in localeOptions"
          :key="option.value"
          type="button"
          role="radio"
          :aria-checked="locale === option.value"
          class="segmented-btn"
          :class="{ active: locale === option.value }"
          :data-testid="`settings-locale-${option.value}`"
          @click="preferencesStore.setLocale(option.value)"
        >
          <Languages :size="16" />
          {{ option.label }}
        </button>
      </div>
    </section>

    <section class="setting-card" aria-labelledby="setting-period-title">
      <header class="setting-header">
        <h2 id="setting-period-title">默认报告期间</h2>
        <p class="muted-text">登录后自动选中的会计期间。</p>
      </header>
      <select
        class="setting-select"
        data-testid="settings-default-period"
        :value="defaultPeriodId ?? ''"
        @change="onDefaultPeriodChange"
      >
        <option value="">不指定（使用系统最新期间）</option>
        <option v-for="period in periods" :key="period.id" :value="period.id">
          {{ period.periodLabel }}
        </option>
      </select>
    </section>
  </main>
</template>

<style scoped>
.page-surface {
  padding: 24px 28px 40px;
}

.settings-page {
  display: flex;
  flex-direction: column;
  gap: var(--density-gap);
}

.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.setting-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: var(--density-card-padding);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-header h2 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
}

.segmented {
  display: inline-flex;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
  width: fit-content;
}

.segmented-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: none;
  background: var(--color-surface);
  color: var(--color-ink);
  cursor: pointer;
  font-size: 13px;
  border-right: 1px solid var(--color-border);
  transition: background 0.15s ease, color 0.15s ease;
}

.segmented-btn:last-child {
  border-right: none;
}

.segmented-btn:hover {
  background: var(--color-surface-soft);
}

.segmented-btn.active {
  background: var(--color-primary);
  color: #fff;
}

.setting-select {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  color: var(--color-ink);
  font-size: 14px;
  width: 100%;
  max-width: 360px;
}
</style>
