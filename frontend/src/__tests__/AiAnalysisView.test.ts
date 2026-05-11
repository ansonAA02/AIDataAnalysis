import { flushPromises, mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import AiAnalysisView from '../views/AiAnalysisView.vue';

vi.mock('../api/ai', () => ({
  askAi: vi.fn(),
}));

import { askAi } from '../api/ai';

const answer = {
  conclusion: '利润下降主要由成本异常增长导致。',
  evidence: '成本从 735 万元升至 910 万元。',
  analysis: '供应商和交付成本上涨，压缩毛利空间。',
  risk: '现金流和利润率承压。',
  recommendation: '复盘供应商成本并收紧费用审批。',
};

describe('AiAnalysisView', () => {
  it('uses a shortcut question and renders structured AI answer', async () => {
    vi.mocked(askAi).mockResolvedValue(answer);
    const wrapper = mount(AiAnalysisView);

    await wrapper.get('[data-testid="shortcut-profit-drop"]').trigger('click');
    await flushPromises();

    expect(askAi).toHaveBeenCalledWith({ periodId: 4, question: '为什么本月利润下降？' });
    expect(wrapper.text()).toContain('结论');
    expect(wrapper.text()).toContain('利润下降主要由成本异常增长导致。');
    expect(wrapper.text()).toContain('建议动作');
    expect(wrapper.text()).toContain('复盘供应商成本并收紧费用审批。');
  });

  it('submits a custom question to the AI ask API', async () => {
    vi.mocked(askAi).mockResolvedValue(answer);
    const wrapper = mount(AiAnalysisView);

    await wrapper.get('textarea').setValue('现金流为什么承压？');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(askAi).toHaveBeenCalledWith({ periodId: 4, question: '现金流为什么承压？' });
    expect(wrapper.text()).toContain('现金流和利润率承压。');
  });

  it('shows loading and disables submit while waiting for AI answer', async () => {
    vi.mocked(askAi).mockReturnValue(new Promise(() => {}));
    const wrapper = mount(AiAnalysisView);

    await wrapper.get('textarea').setValue('预算为什么超支？');
    await wrapper.get('form').trigger('submit.prevent');

    expect(wrapper.get('button[type="submit"]').attributes('disabled')).toBeDefined();
    expect(wrapper.text()).toContain('AI 正在分析');
  });

  it('renders an accessible error state when AI request fails', async () => {
    vi.mocked(askAi).mockRejectedValue(new Error('network down'));
    const wrapper = mount(AiAnalysisView);

    await wrapper.get('[data-testid="shortcut-profit-drop"]').trigger('click');
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toContain('AI 分析请求失败');
  });
});
