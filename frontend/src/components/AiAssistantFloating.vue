<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';
import { Bot, X, Sparkles, Send, Loader2 } from 'lucide-vue-next';
import { askAi } from '../api/ai';

const isOpen = ref(false);
const query = ref('');
const isLoading = ref(false);
const chatHistory = ref<{ role: 'user' | 'ai'; text: string }[]>([]);
const messagesContainer = ref<HTMLElement | null>(null);

const toggleAssistant = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value && chatHistory.value.length === 0) {
    chatHistory.value.push({
      role: 'ai',
      text: '你好！我是你的 AI 财务经营助手。你可以向我询问关于收入、成本、预算偏差或任何经营分析的问题。'
    });
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

watch(() => chatHistory.value.length, scrollToBottom);

const askQuestion = async (text: string) => {
  // Prevent empty or concurrent submissions
  if (!text.trim() || isLoading.value) return;
  
  const userText = text.trim();
  query.value = '';
  
  // Add user message to chat history
  chatHistory.value.push({ role: 'user', text: userText });
  isLoading.value = true;
  
  try {
    const aiMessageIndex = chatHistory.value.length;
    // Add empty AI message placeholder
    chatHistory.value.push({ role: 'ai', text: '' });
    
    // Determine API base URL
    const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '');
    
    // Fetch SSE stream from backend
    const response = await fetch(`${apiBaseUrl}/api/ai/ask/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ periodId: 4, question: userText })
    });

    // Check for HTTP errors
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Get stream reader
    const reader = response.body?.getReader();
    if (!reader) throw new Error('No stream reader available');
    
    isLoading.value = false; // Hide spinner once stream starts
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let eventData = '';
    
    // Read stream chunks continuously
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      
      // Decode chunk and split into lines
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split(/\r?\n/);
      buffer = lines.pop() || ''; // Keep incomplete line in buffer
      
      // Process each line for SSE data
      for (const line of lines) {
        if (line.startsWith('data:')) {
          let text = line.substring(5);
          // Remove single leading space if present
          if (text.startsWith(' ')) text = text.substring(1);
          if (eventData !== '') eventData += '\n';
          eventData += text;
        } else if (line === '') {
          // Empty line indicates end of SSE event
          if (eventData !== '') {
            chatHistory.value[aiMessageIndex].text += eventData;
            eventData = '';
            scrollToBottom();
          }
        }
      }
    }
  } catch (err) {
    console.error('AI Stream Error:', err);
    // Handle network or stream errors
    const aiMessageIndex = chatHistory.value.length - 1;
    if (chatHistory.value[aiMessageIndex] && chatHistory.value[aiMessageIndex].text === '') {
       chatHistory.value[aiMessageIndex].text = '抱歉，暫時無法回答您的問題，請檢查網絡或後端配置。';
    } else {
       chatHistory.value.push({ role: 'ai', text: '\n\n*(連接中斷)*' });
    }
  } finally {
    // Ensure loading state is reset
    isLoading.value = false;
  }
};

const sendQuery = () => {
  askQuestion(query.value);
};

const useShortcut = (shortcut: string) => {
  askQuestion(shortcut);
};

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
          <button class="ai-panel-close" @click="toggleAssistant">
            <X :size="18" />
          </button>
        </div>
        
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
            <div class="chat-message-bubble" v-html="msg.text.replace(/\n/g, '<br>')"></div>
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
</style>