<template>
  <main class="welcome">
    <p class="eyebrow">欢迎使用</p>
    <h2>AI 企业财务经营数据分析决策平台</h2>
    <p class="lead">
      本 MVP 内置最近 12 个月示例财务数据，支持驾驶舱 KPI、趋势与风险、AI 问答与摘要、预算偏差说明以及月度经营报告。所有金额与比率由后端计算，前端仅展示。
    </p>
    <ul class="bullets">
      <li>驾驶舱：总览、趋势图、风险提醒与 AI 经营摘要</li>
      <li>AI 分析：基于当前期间的财务上下文进行单轮问答</li>
      <li>预算偏差：实际与预算对比及 AI 解释</li>
      <li>经营报告：按期间生成的结构化报告</li>
    </ul>
    <div class="actions">
      <RouterLink class="btn primary" to="/">进入驾驶舱</RouterLink>
      <RouterLink class="btn ghost" to="/ai">AI 分析</RouterLink>
      <RouterLink class="btn ghost" to="/budget">预算偏差</RouterLink>
      <RouterLink class="btn ghost" to="/reports">经营报告</RouterLink>
    </div>
  </main>
</template>

<style scoped>
.welcome {
  max-width: 42rem;
  padding: 0.5rem 0 2rem;
}

.eyebrow {
  margin: 0 0 0.5rem;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-muted, #60708a);
}

h2 {
  margin: 0 0 1rem;
  font-size: 1.5rem;
  font-weight: 650;
  color: var(--color-ink, #0f1d33);
  line-height: 1.35;
}

.lead {
  margin: 0 0 1.25rem;
  font-size: 0.95rem;
  line-height: 1.65;
  color: var(--color-muted, #60708a);
}

.bullets {
  margin: 0 0 1.75rem;
  padding-left: 1.25rem;
  color: var(--color-ink, #0f1d33);
  font-size: 0.9rem;
  line-height: 1.7;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.55rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid transparent;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.btn.primary {
  background: var(--color-primary, #1677ff);
  color: #fff;
  border-color: var(--color-primary, #1677ff);
}

.btn.primary:hover {
  filter: brightness(1.05);
}

.btn.ghost {
  background: var(--color-surface, #fff);
  color: var(--color-primary, #1677ff);
  border-color: var(--color-border, #dbe5f1);
}

.btn.ghost:hover {
  background: var(--color-surface-soft, #f4f7fb);
}
</style>
