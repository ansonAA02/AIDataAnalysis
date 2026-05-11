<script setup lang="ts">
// Define props for the shortcut questions list
defineProps<{
  questions: string[];
}>();

// Emit selection event when a shortcut is clicked
const emit = defineEmits<{
  select: [question: string];
}>();

// Generate a stable test id for specific questions
function testId(question: string) {
  if (question.includes('利润下降')) {
    return 'shortcut-profit-drop';
  }
  return undefined;
}
</script>

<template>
  <section class="fin-card shortcut-panel">
    <div class="section-heading">
      <p class="eyebrow">Suggested Questions</p>
      <h2>快捷问题</h2>
    </div>
    <div class="shortcut-list">
      <button
        v-for="(question, i) in questions"
        :key="question"
        class="shortcut-button"
        type="button"
        :data-testid="testId(question)"
        :style="{ '--index': i }"
        @click="emit('select', question)"
      >
        {{ question }}
      </button>
    </div>
  </section>
</template>

<style scoped>
.shortcut-panel {
  padding: 20px 24px;
}

.shortcut-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.shortcut-button {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  min-height: 44px;
  padding: 12px 14px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-ink);
  font-size: 14px;
  font-weight: 500;
  text-align: left;
  transition: transform 220ms cubic-bezier(0.16, 1, 0.3, 1),
              box-shadow 220ms cubic-bezier(0.16, 1, 0.3, 1),
              background-color 220ms cubic-bezier(0.16, 1, 0.3, 1);
  animation: fadeInUp 600ms cubic-bezier(0.16, 1, 0.3, 1) both;
  animation-delay: calc(var(--index) * 80ms);
}

.shortcut-button:hover {
  transform: translateY(-2px);
  background: var(--color-surface-soft);
  box-shadow: var(--shadow-glow);
}

.shortcut-button:active {
  transform: scale(0.98);
}
</style>
