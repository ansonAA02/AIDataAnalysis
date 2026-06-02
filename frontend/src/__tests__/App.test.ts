import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { describe, expect, it, vi } from 'vitest';
import { createRouter, createWebHistory } from 'vue-router';
import App from '../App.vue';

// Mock the periods API so AppLayout's onMounted does not fail in tests.
vi.mock('../api/finance', () => ({
  getPeriods: vi.fn().mockResolvedValue([]),
}));

// Mock the alerts API so AppLayout's bell + sync side effects stay inert in tests.
vi.mock('../api/alerts', () => ({
  syncAlerts: vi.fn().mockResolvedValue([]),
  listAlerts: vi.fn().mockResolvedValue([]),
  getUnreadCount: vi.fn().mockResolvedValue(0),
  updateAlertStatus: vi.fn(),
  markAllAlertsRead: vi.fn().mockResolvedValue(0),
}));

describe('App shell', () => {
  it('renders the product title and primary navigation', async () => {
    // Install a fresh Pinia instance for the period store.
    const pinia = createPinia();
    setActivePinia(pinia);

    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/welcome', component: { template: '<main>Welcome placeholder</main>' } },
        { path: '/', component: { template: '<main>Dashboard placeholder</main>' } },
        { path: '/finance', component: { template: '<main>Finance placeholder</main>' } },
        { path: '/ai', component: { template: '<main>AI placeholder</main>' } },
        { path: '/budget', component: { template: '<main>Budget placeholder</main>' } },
        { path: '/reports', component: { template: '<main>Reports placeholder</main>' } },
        { path: '/risks', component: { template: '<main>Risks placeholder</main>' } },
        { path: '/notes', component: { template: '<main>Notes placeholder</main>' } },
        { path: '/settings', component: { template: '<main>Settings placeholder</main>' } },
      ],
    });

    router.push('/');
    await router.isReady();

    const wrapper = mount(App, {
      global: {
        plugins: [pinia, router],
      },
    });

    expect(wrapper.text()).toContain('AI Finance');
    expect(wrapper.get('[data-testid="nav-dashboard"]').text()).toBe('Dashboard');
    expect(wrapper.get('[data-testid="nav-settings"]').text()).toBe('Settings');
  });
});
