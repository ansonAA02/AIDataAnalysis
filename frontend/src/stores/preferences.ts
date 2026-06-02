// Global preferences store: theme (light/dark/auto) + UI density + locale,
// persisted to localStorage so they survive reloads. Applies CSS classes to
// <html> whenever the values change.
import { defineStore } from 'pinia';
import { computed, ref, watch } from 'vue';

export type ThemeMode = 'light' | 'dark' | 'auto';
export type Density = 'comfortable' | 'compact';
export type Locale = 'zh-CN' | 'zh-TW' | 'en';

const STORAGE_KEY = 'ai-finance:preferences';

// Shape persisted to storage; explicit so future fields are obvious.
interface PersistedPreferences {
  theme: ThemeMode;
  density: Density;
  locale: Locale;
  defaultPeriodId: number | null;
}

// Build a safe default for SSR and first-run users.
function defaults(): PersistedPreferences {
  return {
    theme: 'light',
    density: 'comfortable',
    locale: 'zh-CN',
    defaultPeriodId: null,
  };
}

// Load persisted prefs, gracefully falling back when storage is unavailable.
function load(): PersistedPreferences {
  if (typeof window === 'undefined') {
    return defaults();
  }
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return defaults();
  }
  try {
    const parsed = JSON.parse(raw) as Partial<PersistedPreferences>;
    return { ...defaults(), ...parsed };
  } catch {
    return defaults();
  }
}

// Persist the entire object (small enough that we don't bother diffing).
function save(prefs: PersistedPreferences): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs));
}

// Resolve the effective theme when mode is "auto" (follows system preference).
function resolveEffectiveTheme(mode: ThemeMode): 'light' | 'dark' {
  if (mode !== 'auto') {
    return mode;
  }
  if (typeof window === 'undefined' || !window.matchMedia) {
    return 'light';
  }
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

// Apply the resolved theme + density classes to <html>.
function applyToDocument(theme: 'light' | 'dark', density: Density): void {
  if (typeof document === 'undefined') {
    return;
  }
  const root = document.documentElement;
  root.classList.toggle('theme-dark', theme === 'dark');
  root.classList.toggle('theme-light', theme === 'light');
  root.classList.toggle('density-compact', density === 'compact');
  root.setAttribute('data-theme', theme);
}

export const usePreferencesStore = defineStore('preferences', () => {
  const initial = load();

  // Reactive state mirrors the persisted shape.
  const theme = ref<ThemeMode>(initial.theme);
  const density = ref<Density>(initial.density);
  const locale = ref<Locale>(initial.locale);
  const defaultPeriodId = ref<number | null>(initial.defaultPeriodId);

  // Effective light/dark used for actually painting the UI.
  const effectiveTheme = computed(() => resolveEffectiveTheme(theme.value));

  // Push current state into the document so visuals stay in sync.
  function syncDocument() {
    applyToDocument(effectiveTheme.value, density.value);
  }

  // Persist current state to localStorage.
  function persist() {
    save({
      theme: theme.value,
      density: density.value,
      locale: locale.value,
      defaultPeriodId: defaultPeriodId.value,
    });
  }

  // Mutators exposed to UI components.
  function setTheme(next: ThemeMode) {
    theme.value = next;
  }
  function setDensity(next: Density) {
    density.value = next;
  }
  function setLocale(next: Locale) {
    locale.value = next;
  }
  function setDefaultPeriodId(next: number | null) {
    defaultPeriodId.value = next;
  }
  function reset() {
    const fresh = defaults();
    theme.value = fresh.theme;
    density.value = fresh.density;
    locale.value = fresh.locale;
    defaultPeriodId.value = fresh.defaultPeriodId;
  }

  // Re-apply + persist whenever any field changes.
  watch([theme, density, locale, defaultPeriodId], () => {
    syncDocument();
    persist();
  });

  // Listen to system theme changes when in auto mode.
  if (typeof window !== 'undefined' && window.matchMedia) {
    const mql = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => {
      if (theme.value === 'auto') {
        syncDocument();
      }
    };
    if (mql.addEventListener) {
      mql.addEventListener('change', handler);
    } else if (mql.addListener) {
      mql.addListener(handler);
    }
  }

  // Initial paint on store creation.
  syncDocument();

  return {
    theme,
    density,
    locale,
    defaultPeriodId,
    effectiveTheme,
    setTheme,
    setDensity,
    setLocale,
    setDefaultPeriodId,
    reset,
  };
});
