<template>
  <main class="notes-page page-surface">
    <header class="page-hero">
      <div>
        <p class="eyebrow">Data Notes</p>
        <h1>数据笔记</h1>
        <p class="muted-text">此区域预留给管理层对异常期间、假设与复盘结论的文字记录（MVP 占位）。</p>
      </div>
    </header>
    <section class="fin-card" style="padding: 24px">
      <p style="margin: 0; color: var(--color-muted)">
        后续可接入轻量笔记 API 或与导出报告联动；当前版本不写入数据库。
      </p>
    </section>
  </main>
</template>

<style scoped>
.page-surface {
  padding: 24px 28px 40px;
}

.page-hero h1 {
  margin: 6px 0 8px;
}
</style>
