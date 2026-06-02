import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AiAssistantFloating from '../components/AiAssistantFloating.vue';

// Mock every export the floating assistant uses from the AI client module.
vi.mock('../api/ai', () => ({
  streamAiAnswer: vi.fn(),
  appendConversationTurn: vi.fn().mockResolvedValue({
    id: 1,
    title: 'mock',
    periodId: 4,
    favorited: false,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
    messageCount: 2,
    preview: 'mock',
    messages: [],
  }),
  listConversations: vi.fn().mockResolvedValue([]),
  getConversationDetail: vi.fn(),
  toggleConversationFavorite: vi.fn(),
  deleteConversation: vi.fn(),
}));

import { streamAiAnswer, appendConversationTurn, listConversations } from '../api/ai';

// Open the floating assistant with a fresh Pinia plugin for each test.
async function openAssistant() {
  const pinia = createPinia();
  setActivePinia(pinia);
  const wrapper = mount(AiAssistantFloating, {
    global: { plugins: [pinia] },
  });
  await wrapper.get('.ai-fab').trigger('click');
  return wrapper;
}

describe('AiAssistantFloating', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });
  it('streams assistant replies through the shared AI client', async () => {
    vi.mocked(streamAiAnswer).mockImplementation(async (_request, onChunk) => {
      onChunk('利润下降主要由成本上涨导致。');
      onChunk('\n建议优先复盘供应商成本。');
    });

    const wrapper = await openAssistant();
    await wrapper.get('.shortcut-chip').trigger('click');
    await flushPromises();

    expect(streamAiAnswer).toHaveBeenCalledWith(
      { periodId: 4, question: '本月最大的预算偏差是什么？' },
      expect.any(Function),
    );
    expect(wrapper.text()).toContain('利润下降主要由成本上涨导致。');
    expect(wrapper.text()).toContain('建议优先复盘供应商成本。');
  });

  it('renders AI content as escaped text instead of raw HTML', async () => {
    vi.mocked(streamAiAnswer).mockImplementation(async (_request, onChunk) => {
      onChunk('<script>alert("xss")</script>\n安全文本');
    });

    const wrapper = await openAssistant();
    await wrapper.get('.shortcut-chip').trigger('click');
    await flushPromises();

    const bubbles = wrapper.findAll('.chat-message-bubble');
    const lastBubble = bubbles[bubbles.length - 1];

    expect(lastBubble.text()).toContain('<script>alert("xss")</script>');
    expect(lastBubble.html()).not.toContain('<script>alert("xss")</script>');
    expect(lastBubble.text()).toContain('安全文本');
  });

  it('persists each completed Q&A turn through the conversation API', async () => {
    vi.mocked(streamAiAnswer).mockImplementation(async (_request, onChunk) => {
      onChunk('利润下降主要由成本上涨导致。');
    });

    const wrapper = await openAssistant();
    await wrapper.get('.shortcut-chip').trigger('click');
    await flushPromises();

    expect(appendConversationTurn).toHaveBeenCalledWith({
      conversationId: null,
      periodId: 4,
      question: '本月最大的预算偏差是什么？',
      answer: '利润下降主要由成本上涨导致。',
    });
  });

  it('opens the history drawer and lists stored conversations', async () => {
    vi.mocked(listConversations).mockResolvedValue([
      {
        id: 7,
        title: '上月利润下降原因',
        periodId: 4,
        favorited: true,
        createdAt: '2025-01-01T00:00:00Z',
        updatedAt: '2025-01-02T00:00:00Z',
        messageCount: 4,
        preview: '主要由成本增长导致…',
      },
    ]);

    const wrapper = await openAssistant();
    // Click the History icon button (first action in the header).
    await wrapper.get('.ai-panel-icon-btn').trigger('click');
    await flushPromises();

    expect(listConversations).toHaveBeenCalledWith(false);
    expect(wrapper.text()).toContain('上月利润下降原因');
    expect(wrapper.text()).toContain('主要由成本增长导致…');
  });
});
