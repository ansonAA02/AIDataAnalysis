<script setup lang="ts">
// Data import view: 3-step wizard for uploading finance metric CSV files.
// Step 1 pick file -> Step 2 preview validation results -> Step 3 commit.
import { computed, ref } from 'vue';
import { Upload, FileText, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-vue-next';
import {
  commitFinanceMetrics,
  previewFinanceMetrics,
  type ImportCommitResponse,
  type ImportPreviewResponse,
} from '../api/dataImport';

// Currently selected file (null until user picks).
const file = ref<File | null>(null);
// Preview response from the backend, populated after step 1.
const preview = ref<ImportPreviewResponse | null>(null);
// Commit response shown on step 3.
const commitResult = ref<ImportCommitResponse | null>(null);
// Loading state shared by preview / commit calls.
const loading = ref(false);
// User-friendly error banner.
const errorMessage = ref('');

// Active wizard step; derived from state to keep template simple.
const step = computed<1 | 2 | 3>(() => {
  if (commitResult.value) return 3;
  if (preview.value) return 2;
  return 1;
});

// Handle the <input type="file"> change event.
function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  file.value = input.files && input.files[0] ? input.files[0] : null;
  preview.value = null;
  commitResult.value = null;
  errorMessage.value = '';
}

// Step 1 -> 2: send file to backend and render the preview table.
async function runPreview() {
  if (!file.value) {
    errorMessage.value = '请先选择 CSV 文件。';
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  try {
    preview.value = await previewFinanceMetrics(file.value);
  } catch (err) {
    const message = err instanceof Error ? err.message : '预览失败';
    errorMessage.value = `预览失败：${message}`;
  } finally {
    loading.value = false;
  }
}

// Step 2 -> 3: re-upload the file and commit valid rows.
async function runCommit() {
  if (!file.value) {
    errorMessage.value = '会话已重置，请重新选择文件。';
    return;
  }
  loading.value = true;
  errorMessage.value = '';
  try {
    commitResult.value = await commitFinanceMetrics(file.value);
  } catch (err) {
    const message = err instanceof Error ? err.message : '入库失败';
    errorMessage.value = `入库失败：${message}`;
  } finally {
    loading.value = false;
  }
}

// Reset the wizard back to step 1.
function reset() {
  file.value = null;
  preview.value = null;
  commitResult.value = null;
  errorMessage.value = '';
}
</script>

<template>
  <main class="import-page page-surface">
    <header class="page-hero">
      <div>
        <p class="eyebrow">Data Import</p>
        <h1>财务指标 CSV 导入</h1>
        <p class="muted-text">
          表头需包含：periodLabel, metricCode, metricName, metricCategory, actualValue, unit
        </p>
      </div>
    </header>

    <ol class="step-indicator" aria-label="导入步骤">
      <li :class="{ active: step >= 1, done: step > 1 }">
        <span class="step-dot">1</span> 选择文件
      </li>
      <li :class="{ active: step >= 2, done: step > 2 }">
        <span class="step-dot">2</span> 预览校验
      </li>
      <li :class="{ active: step >= 3 }">
        <span class="step-dot">3</span> 完成入库
      </li>
    </ol>

    <p v-if="errorMessage" class="state-card state-card--warning" role="status">
      {{ errorMessage }}
    </p>

    <section v-if="step === 1" class="setting-card">
      <header class="setting-header">
        <h2>选择 CSV 文件</h2>
        <p class="muted-text">支持 UTF-8 编码的逗号分隔值文件。</p>
      </header>
      <label class="file-drop">
        <input
          type="file"
          accept=".csv,text/csv"
          data-testid="import-file-input"
          @change="onFileChange"
        />
        <Upload :size="22" />
        <span v-if="file" class="file-name">{{ file.name }}</span>
        <span v-else>点击选择 CSV 文件</span>
      </label>
      <div class="action-row">
        <button
          type="button"
          class="fin-btn fin-btn--primary"
          data-testid="import-run-preview"
          :disabled="!file || loading"
          @click="runPreview"
        >
          <FileText :size="14" /> 解析预览
        </button>
      </div>
    </section>

    <section v-else-if="step === 2 && preview" class="setting-card">
      <header class="setting-header">
        <h2>预览校验结果</h2>
        <p class="muted-text">
          共 {{ preview.totalRows }} 行，
          <span class="ok">有效 {{ preview.validRows }}</span>，
          <span class="bad">错误 {{ preview.invalidRows }}</span>
        </p>
      </header>
      <div class="preview-table-wrap">
        <table class="preview-table" data-testid="import-preview-table">
          <thead>
            <tr>
              <th>行</th>
              <th>状态</th>
              <th>periodLabel</th>
              <th>metricCode</th>
              <th>metricName</th>
              <th>actualValue</th>
              <th>unit</th>
              <th>错误</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in preview.rows"
              :key="row.rowNumber"
              :class="{ 'row-bad': !row.valid }"
            >
              <td>{{ row.rowNumber }}</td>
              <td>
                <CheckCircle2 v-if="row.valid" :size="14" class="ok" />
                <AlertCircle v-else :size="14" class="bad" />
              </td>
              <td>{{ row.periodLabel ?? '—' }}</td>
              <td>{{ row.metricCode ?? '—' }}</td>
              <td>{{ row.metricName ?? '—' }}</td>
              <td>{{ row.actualValue ?? '—' }}</td>
              <td>{{ row.unit ?? '—' }}</td>
              <td class="bad">{{ row.error ?? '' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="action-row">
        <button type="button" class="fin-btn fin-btn--ghost" @click="reset">
          <RefreshCw :size="14" /> 重新选择
        </button>
        <button
          type="button"
          class="fin-btn fin-btn--primary"
          data-testid="import-run-commit"
          :disabled="loading || preview.validRows === 0"
          @click="runCommit"
        >
          <Upload :size="14" /> 确认入库（{{ preview.validRows }} 行）
        </button>
      </div>
    </section>

    <section v-else-if="step === 3 && commitResult" class="setting-card">
      <header class="setting-header">
        <h2>入库完成 ✅</h2>
        <p class="muted-text">已写入数据库，可在 Dashboard / Finance 页面查看最新值。</p>
      </header>
      <ul class="result-stats">
        <li><strong>{{ commitResult.processedRows }}</strong><span>处理行数</span></li>
        <li><strong class="ok">{{ commitResult.insertedRows }}</strong><span>新增</span></li>
        <li><strong class="ok">{{ commitResult.updatedRows }}</strong><span>更新</span></li>
        <li><strong class="bad">{{ commitResult.skippedRows }}</strong><span>跳过</span></li>
      </ul>
      <div class="action-row">
        <button type="button" class="fin-btn fin-btn--primary" @click="reset">
          <Upload :size="14" /> 再导入一份
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.page-surface {
  padding: 24px 28px 40px;
}

.import-page {
  display: flex;
  flex-direction: column;
  gap: var(--density-gap, 24px);
}

.step-indicator {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 12px;
}

.step-indicator li {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  font-size: 13px;
  color: var(--color-muted);
}

.step-indicator li.active {
  color: var(--color-ink);
  border-color: var(--color-primary);
}

.step-indicator li.done {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.step-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--color-surface-soft);
  font-weight: 600;
}

.step-indicator li.active .step-dot {
  background: var(--color-primary);
  color: #fff;
}

.setting-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: var(--density-card-padding, 24px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-header h2 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
}

.file-drop {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  padding: 32px;
  border: 1px dashed var(--color-border);
  border-radius: 12px;
  cursor: pointer;
  color: var(--color-muted);
  transition: border-color 0.15s ease, color 0.15s ease;
}

.file-drop:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.file-drop input[type='file'] {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.file-name {
  font-weight: 600;
  color: var(--color-ink);
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.preview-table-wrap {
  overflow-x: auto;
  max-height: 420px;
  overflow-y: auto;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.preview-table th,
.preview-table td {
  padding: 8px 12px;
  border-bottom: 1px solid var(--color-border);
  text-align: left;
}

.preview-table thead {
  background: var(--color-surface-soft);
  position: sticky;
  top: 0;
}

.row-bad {
  background: var(--color-danger-bg);
}

.ok { color: var(--color-success); }
.bad { color: var(--color-danger); }

.result-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.result-stats li {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: var(--color-surface-soft);
  border-radius: 10px;
}

.result-stats strong {
  font-size: 24px;
  font-weight: 700;
}

.result-stats span {
  font-size: 12px;
  color: var(--color-muted);
}
</style>
