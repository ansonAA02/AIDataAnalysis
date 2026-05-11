import { mount } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import App from '../App.vue';

describe('App shell', () => {
  it('renders the product title and primary navigation', async () => {
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
      ],
    });

    router.push('/');
    await router.isReady();

    const wrapper = mount(App, {
      global: {
        plugins: [router],
      },
    });

    expect(wrapper.text()).toContain('AI Finance');
    expect(wrapper.get('[data-testid="nav-welcome"]').text()).toBe('欢迎');
    expect(wrapper.get('[data-testid="nav-dashboard"]').text()).toBe('Dashboard');
  });
});
