<script setup lang="ts">
// Floating AI assistant: subscribes to the global Pinia period store so the
// chat is always scoped to whichever period the user picked in the top bar.
// Now also persists each Q&A pair to the backend so users can revisit past
// sessions through a History drawer (list / favorite / delete / reload).
import { ref, nextTick, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { Bot, X, Sparkles, Send, Loader2, History, Star, Trash2, Plus } from 'lucide-vue-next';
import {
  appendConversationTurn,
  deleteConversation,
  getConversationDetail,
  listConversations,
  streamAiAnswer,
  toggleConversationFavorite,
} from '../api/ai';
import { usePeriodStore } from '../stores/period';
import type { AiConversationSummary } from '../types/finance';

type ChatMessage = {
  role: 'user' | 'ai';
  text: string;
};

// Fallback period id used only when the store has no period yet.
const DEFAULT_PERIOD_ID = 4;
const ASSISTANT_GREETING =
  '你好！我是你的 AI 财务经营助手。你可以向我询问关于收入、成本、预算偏差或任何经营分析的问题。';
const STREAM_ERROR_MESSAGE = '抱歉，暂时无法回答您的问题，请检查网络或后端配置。';
const STREAM_INTERRUPTED_MESSAGE = '\n\n(连接中断)';

const periodStore = usePeriodStore();
const { currentPeriodId } = storeToRefs(periodStore);

// Resolve the active period id with a safe fallback.
function resolvePeriodId(): number {
  return currentPeriodId.value ?? DEFAULT_PERIOD_ID;
}

const isOpen = ref(false);
const query = ref('');
const isLoading = ref(false);
const chatHistory = ref<ChatMessage[]>([]);
const messagesContainer = ref<HTMLElement | null>(null);

// History drawer state: open flag, filter, list cache, and active session id.
const isHistoryOpen = ref(false);
const showFavoritesOnly = ref(false);
const conversations = ref<AiConversationSummary[]>([]);
const isHistoryLoading = ref(false);
const activeConversationId = ref<number | null>(null);

// Seed the assistant with a greeting when the panel opens for the first time.
const toggleAssistant = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value && chatHistory.value.length === 0) {
    chatHistory.value.push({
      role: 'ai',
      text: ASSISTANT_GREETING
    });
  }
};

// Keep the newest chat message visible after the DOM updates.
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

watch(() => chatHistory.value.length, scrollToBottom);

// Append streamed text to the placeholder AI message.
const appendAiChunk = (messageIndex: number, chunk: string) => {
  const targetMessage = chatHistory.value[messageIndex];
  if (targetMessage) {
    targetMessage.text += chunk;
  }
};

// Replace an empty placeholder with a readable fallback error.
const applyAiErrorState = (messageIndex: number) => {
  const targetMessage = chatHistory.value[messageIndex];
  if (targetMessage && targetMessage.text === '') {
    targetMessage.text = STREAM_ERROR_MESSAGE;
    return;
  }

  chatHistory.value.push({ role: 'ai', text: STREAM_INTERRUPTED_MESSAGE });
};

// Submit a chat question through the shared AI streaming client.
const askQuestion = async (text: string) => {
  if (!text.trim() || isLoading.value) return;

  const userText = text.trim();
  query.value = '';

  chatHistory.value.push({ role: 'user', text: userText });
  isLoading.value = true;
  const aiMessageIndex = chatHistory.value.length;
  chatHistory.value.push({ role: 'ai', text: '' });

  let assistantText = '';
  let streamFailed = false;

  try {
    await streamAiAnswer(
      { periodId: resolvePeriodId(), question: userText },
      (chunk) => {
        isLoading.value = false;
        assistantText += chunk;
        appendAiChunk(aiMessageIndex, chunk);
        scrollToBottom();
      },
    );
  } catch (err) {
    console.error('AI Stream Error:', err);
    streamFailed = true;
    applyAiErrorState(aiMessageIndex);
  } finally {
    isLoading.value = false;
  }

  // Persist the turn so users can revisit it later (skip on hard failure).
  if (!streamFailed && assistantText.trim().length > 0) {
    try {
      const detail = await appendConversationTurn({
        conversationId: activeConversationId.value,
        periodId: resolvePeriodId(),
        question: userText,
        answer: assistantText,
      });
      activeConversationId.value = detail.id;
    } catch (saveErr) {
      console.warn('Failed to save AI conversation turn:', saveErr);
    }
  }
};

// Submit the current textarea value.
const sendQuery = () => {
  askQuestion(query.value);
};

// Reuse the same submit flow for shortcut prompts.
const useShortcut = (shortcut: string) => {
  askQuestion(shortcut);
};

// Open the history drawer and refresh the list of stored conversations.
async function openHistory() {
  isHistoryOpen.value = true;
  await refreshHistory();
}

// Close the history drawer without affecting the active chat session.
function closeHistory() {
  isHistoryOpen.value = false;
}

// Refresh the conversation list using the current favorites filter.
async function refreshHistory() {
  isHistoryLoading.value = true;
  try {
    conversations.value = await listConversations(showFavoritesOnly.value);
  } catch (err) {
    console.warn('Failed to load AI conversation history:', err);
    conversations.value = [];
  } finally {
    isHistoryLoading.value = false;
  }
}

// Switch between "all" and "favorites only" filter modes.
async function setFavoritesFilter(value: boolean) {
  if (showFavoritesOnly.value === value) return;
  showFavoritesOnly.value = value;
  await refreshHistory();
}

// Reset the chat panel to a fresh, unsaved session.
function startNewConversation() {
  activeConversationId.value = null;
  chatHistory.value = [{ role: 'ai', text: ASSISTANT_GREETING }];
  closeHistory();
}

// Load a stored conversation back into the chat panel.
async function loadConversation(id: number) {
  try {
    const detail = await getConversationDetail(id);
    activeConversationId.value = detail.id;
    chatHistory.value = detail.messages.map((message) => ({
      role: message.role === 'assistant' ? 'ai' : 'user',
      text: message.content,
    }));
    closeHistory();
  } catch (err) {
    console.warn('Failed to load AI conversation:', err);
  }
}

// Toggle the favorited flag on a conversation row.
async function favoriteConversation(summary: AiConversationSummary) {
  try {
    const updated = await toggleConversationFavorite(summary.id);
    const index = conversations.value.findIndex((item) => item.id === summary.id);
    if (showFavoritesOnly.value && !updated.favorited) {
      conversations.value.splice(index, 1);
    } else if (index >= 0) {
      conversations.value[index] = updated;
    }
  } catch (err) {
    console.warn('Failed to toggle favorite:', err);
  }
}

// Delete a conversation and refresh the list; reset active session if needed.
async function removeConversation(summary: AiConversationSummary) {
  try {
    await deleteConversation(summary.id);
    conversations.value = conversations.value.filter((item) => item.id !== summary.id);
    if (activeConversationId.value === summary.id) {
      startNewConversation();
    }
  } catch (err) {
    console.warn('Failed to delete conversation:', err);
  }
}

const shortcuts = [
  '本月最大的预算偏差是什么？',
  '为什么本月净利润下降？',
  '总结当前的经营风险。'
];
</script>

<template>
  <div class="ai-assistant-wrapper">
    <!-- Floating Button -->
    <button 
      class="ai-fab" 
      :class="{ 'ai-fab--open': isOpen }"
      @click="toggleAssistant"
      aria-label="AI Assistant"
    >
      <X v-if="isOpen" :size="24" />
      <Sparkles v-else :size="24" />
    </button>

    <!-- Floating Panel -->
    <transition name="slide-up">
      <div v-if="isOpen" class="ai-panel">
        <div class="ai-panel-header">
          <div class="ai-panel-title">
            <Bot :size="20" />
            <span>AI 财务助手</span>
          </div>
          <div class="ai-panel-header-actions">
            <button
              class="ai-panel-icon-btn"
              type="button"
              :title="isHistoryOpen ? '关闭历史' : '查看对话历史'"
              :aria-pressed="isHistoryOpen"
              @click="isHistoryOpen ? closeHistory() : openHistory()"
            >
              <History :size="16" />
            </button>
            <button
              class="ai-panel-icon-btn"
              type="button"
              title="开启新对话"
              @click="startNewConversation"
            >
              <Plus :size="16" />
            </button>
            <button class="ai-panel-close" @click="toggleAssistant" title="关闭助手">
              <X :size="18" />
            </button>
          </div>
        </div>

        <!-- History drawer overlays the chat area while open. -->
        <transition name="slide-fade">
          <div v-if="isHistoryOpen" class="ai-history-drawer">
            <div class="ai-history-filters">
              <button
                class="ai-history-tab"
                :class="{ 'ai-history-tab--active': !showFavoritesOnly }"
                type="button"
                @click="setFavoritesFilter(false)"
              >
                全部
              </button>
              <button
                class="ai-history-tab"
                :class="{ 'ai-history-tab--active': showFavoritesOnly }"
                type="button"
                @click="setFavoritesFilter(true)"
              >
                <Star :size="12" />
                收藏
              </button>
            </div>

            <div v-if="isHistoryLoading" class="ai-history-empty">加载中…</div>
            <div v-else-if="conversations.length === 0" class="ai-history-empty">
              {{ showFavoritesOnly ? '还没有收藏的对话' : '还没有历史对话' }}
            </div>
            <ul v-else class="ai-history-list">
              <li
                v-for="conversation in conversations"
                :key="conversation.id"
                class="ai-history-item"
                :class="{ 'ai-history-item--active': conversation.id === activeConversationId }"
              >
                <button
                  type="button"
                  class="ai-history-item-main"
                  @click="loadConversation(conversation.id)"
                >
                  <div class="ai-history-item-title">{{ conversation.title }}</div>
                  <div class="ai-history-item-preview">{{ conversation.preview }}</div>
                  <div class="ai-history-item-meta">
                    {{ conversation.messageCount }} 条 · {{ new Date(conversation.updatedAt).toLocaleString('zh-CN') }}
                  </div>
                </button>
                <div class="ai-history-item-actions">
                  <button
                    type="button"
                    class="ai-history-item-action"
                    :class="{ 'ai-history-item-action--on': conversation.favorited }"
                    :title="conversation.favorited ? '取消收藏' : '收藏'"
                    @click.stop="favoriteConversation(conversation)"
                  >
                    <Star :size="14" />
                  </button>
                  <button
                    type="button"
                    class="ai-history-item-action ai-history-item-action--danger"
                    title="删除"
                    @click.stop="removeConversation(conversation)"
                  >
                    <Trash2 :size="14" />
                  </button>
                </div>
              </li>
            </ul>
          </div>
        </transition>
        
        <div class="ai-panel-messages" ref="messagesContainer">
          <div 
            v-for="(msg, index) in chatHistory" 
            :key="index"
            class="chat-message"
            :class="`chat-message--${msg.role}`"
          >
            <div class="chat-message-avatar">
              <Bot v-if="msg.role === 'ai'" :size="16" />
              <span v-else>ME</span>
            </div>
            <div class="chat-message-bubble">{{ msg.text }}</div>
          </div>
          
          <div v-if="isLoading" class="chat-message chat-message--ai">
            <div class="chat-message-avatar"><Bot :size="16" /></div>
            <div class="chat-message-bubble chat-message-bubble--loading">
              <Loader2 class="spinner" :size="16" />
              <span>正在思考...</span>
            </div>
          </div>
        </div>

        <div class="ai-panel-shortcuts" v-if="chatHistory.length <= 1">
          <button 
            v-for="shortcut in shortcuts" 
            :key="shortcut"
            class="shortcut-chip"
            @click="useShortcut(shortcut)"
          >
            {{ shortcut }}
          </button>
        </div>

        <div class="ai-panel-input">
          <textarea 
            v-model="query" 
            placeholder="询问财务或经营数据..."
            @keydown.enter.prevent="sendQuery"
            rows="1"
          ></textarea>
          <button class="send-btn" :disabled="!query.trim() || isLoading" @click="sendQuery">
            <Send :size="16" />
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.ai-assistant-wrapper {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.ai-fab {
  width: 56px;
  height: 56px;
  border-radius: 28px;
  background: var(--color-accent-purple);
  color: #fff;
  border: none;
  box-shadow: var(--shadow-floating);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.ai-fab:hover {
  transform: scale(1.05) translateY(-2px);
  background: #7C3AED;
}

.ai-fab--open {
  background: var(--color-surface);
  color: var(--color-ink);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--color-border);
}

.ai-panel {
  position: absolute;
  bottom: 76px;
  right: 0;
  width: 400px;
  height: 600px;
  max-height: calc(100vh - 120px);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: var(--shadow-floating);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transform-origin: bottom right;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

.ai-panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-surface-soft);
}

.ai-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--color-accent-purple);
}

.ai-panel-close {
  background: none;
  border: none;
  color: var(--color-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: grid;
  place-items: center;
}

.ai-panel-close:hover {
  background: var(--color-border);
  color: var(--color-ink);
}

.ai-panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.chat-message--user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-message-avatar {
  width: 28px;
  height: 28px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  font-size: 10px;
  font-weight: 700;
}

.chat-message--ai .chat-message-avatar {
  background: var(--color-accent-purple-bg);
  color: var(--color-accent-purple);
}

.chat-message--user .chat-message-avatar {
  background: var(--color-ink);
  color: #fff;
}

.chat-message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-message--ai .chat-message-bubble {
  background: var(--color-surface-soft);
  color: var(--color-ink);
  border-top-left-radius: 4px;
}

.chat-message--user .chat-message-bubble {
  background: var(--color-primary);
  color: #fff;
  border-top-right-radius: 4px;
}

.chat-message-bubble--loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-muted) !important;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-panel-shortcuts {
  padding: 0 20px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.shortcut-chip {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.shortcut-chip:hover {
  background: var(--color-accent-purple-bg);
  color: var(--color-accent-purple);
  border-color: var(--color-accent-purple);
}

.ai-panel-input {
  padding: 16px;
  border-top: 1px solid var(--color-border);
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: var(--color-surface);
}

.ai-panel-input textarea {
  flex: 1;
  max-height: 120px;
  resize: none;
  background: var(--color-surface-soft);
  border: 1px solid transparent;
}

.ai-panel-input textarea:focus {
  border-color: var(--color-accent-purple);
  box-shadow: 0 0 0 1px var(--color-accent-purple);
  background: var(--color-surface);
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  background: var(--color-accent-purple);
  color: #fff;
  border: none;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #7C3AED;
}

.send-btn:disabled {
  background: var(--color-border);
  color: var(--color-muted);
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .ai-panel {
    width: calc(100vw - 32px);
    right: -16px;
  }
}

/* History drawer + header action buttons. */
.ai-panel-header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ai-panel-icon-btn {
  background: none;
  border: none;
  color: var(--color-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: grid;
  place-items: center;
}

.ai-panel-icon-btn:hover,
.ai-panel-icon-btn[aria-pressed='true'] {
  background: var(--color-accent-purple-bg);
  color: var(--color-accent-purple);
}

.ai-history-drawer {
  position: absolute;
  top: 56px;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-surface);
  display: flex;
  flex-direction: column;
  border-top: 1px solid var(--color-border);
  z-index: 5;
}

.ai-history-filters {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.ai-history-tab {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-muted);
  font-size: 12px;
  cursor: pointer;
}

.ai-history-tab--active {
  background: var(--color-accent-purple-bg);
  color: var(--color-accent-purple);
  border-color: var(--color-accent-purple);
}

.ai-history-empty {
  padding: 32px 16px;
  text-align: center;
  color: var(--color-muted);
  font-size: 13px;
}

.ai-history-list {
  list-style: none;
  margin: 0;
  padding: 8px;
  overflow-y: auto;
  flex: 1;
}

.ai-history-item {
  display: flex;
  align-items: stretch;
  gap: 4px;
  border-radius: 8px;
  margin-bottom: 4px;
  border: 1px solid transparent;
}

.ai-history-item:hover {
  background: var(--color-surface-soft);
}

.ai-history-item--active {
  border-color: var(--color-accent-purple);
  background: var(--color-accent-purple-bg);
}

.ai-history-item-main {
  flex: 1;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px 10px;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.ai-history-item-title {
  font-weight: 600;
  font-size: 13px;
  color: var(--color-ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ai-history-item-preview {
  font-size: 12px;
  color: var(--color-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ai-history-item-meta {
  font-size: 11px;
  color: var(--color-muted);
  margin-top: 2px;
}

.ai-history-item-actions {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px;
}

.ai-history-item-action {
  background: none;
  border: none;
  color: var(--color-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: grid;
  place-items: center;
}

.ai-history-item-action:hover {
  background: var(--color-border);
  color: var(--color-ink);
}

.ai-history-item-action--on {
  color: #f59e0b;
}

.ai-history-item-action--danger:hover {
  background: #fee2e2;
  color: #b91c1c;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.18s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
