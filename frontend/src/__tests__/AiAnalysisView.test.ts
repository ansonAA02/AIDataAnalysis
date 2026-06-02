import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AiAnalysisView from '../views/AiAnalysisView.vue';

vi.mock('../api/ai', () => ({
  askAi: vi.fn(),
  streamAiAnswer: vi.fn(),
}));

import { streamAiAnswer } from '../api/ai';

// Mount the AI view with a fresh Pinia instance per test. The view falls
// back to DEFAULT_PERIOD_ID = 4 when the store has no preloaded period.
function mountAi() {
  const pinia = createPinia();
  setActivePinia(pinia);
  return mount(AiAnalysisView, {
    global: { plugins: [pinia] },
  });
}

const answer = {
  conclusion: '利润下降主要由成本异常增长导致。',
  evidence: '成本从 735 万元升至 910 万元。',
  analysis: '供应商和交付成本上涨，压缩毛利空间。',
  risk: '现金流和利润率承压。',
  recommendation: '复盘供应商成本并收紧费用审批。',
};

describe('AiAnalysisView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('uses a shortcut question and renders structured AI answer', async () => {
    vi.mocked(streamAiAnswer).mockImplementation(async (_request, onChunk) => {
      onChunk(answer.conclusion);
      onChunk(answer.recommendation);
    });
    const wrapper = mountAi();

    await wrapper.get('[data-testid="shortcut-profit-drop"]').trigger('click');
    await flushPromises();

    expect(streamAiAnswer).toHaveBeenCalledWith(
      { periodId: 4, question: '为什么本月利润下降？' },
      expect.any(Function),
    );
    expect(wrapper.text()).toContain('结论');
    expect(wrapper.text()).toContain('利润下降主要由成本异常增长导致。');
    expect(wrapper.text()).toContain('复盘供应商成本并收紧费用审批。');
  });

  it('submits a custom question to the AI ask API', async () => {
    vi.mocked(streamAiAnswer).mockImplementation(async (_request, onChunk) => {
      onChunk(answer.risk);
    });
    const wrapper = mountAi();

    await wrapper.get('textarea').setValue('现金流为什么承压？');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(streamAiAnswer).toHaveBeenCalledWith(
      { periodId: 4, question: '现金流为什么承压？' },
      expect.any(Function),
    );
    expect(wrapper.text()).toContain('现金流和利润率承压。');
  });

  it('shows loading and disables submit while waiting for AI answer', async () => {
    vi.mocked(streamAiAnswer).mockReturnValue(new Promise(() => {}));
    const wrapper = mountAi();

    await wrapper.get('textarea').setValue('预算为什么超支？');
    await wrapper.get('form').trigger('submit.prevent');

    expect(wrapper.get('button[type="submit"]').attributes('disabled')).toBeDefined();
    expect(wrapper.text()).toContain('AI 正在分析');
  });

  it('renders an accessible error state when AI request fails', async () => {
    vi.mocked(streamAiAnswer).mockRejectedValue(new Error('network down'));
    const wrapper = mountAi();

    await wrapper.get('[data-testid="shortcut-profit-drop"]').trigger('click');
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('AI 分析请求失败');
  });
});
