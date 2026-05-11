<script setup lang="ts">
import type { BudgetVariance } from '../types/finance';

defineProps<{
  variances: BudgetVariance[];
}>();

function formatAmount(value: number, unit: string) {
  return `${value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${unit}`;
}

function formatRate(value: number) {
  return `${value.toFixed(2)}%`;
}

function varianceLabel(value: number) {
  if (value > 0) {
    return '超支';
  }

  if (value < 0) {
    return '节余';
  }

  return '持平';
}
</script>

<template>
  <section class="panel-card variance-table-card">
    <div class="section-heading">
      <p class="eyebrow">Variance Matrix</p>
      <h2>预算偏差明细</h2>
    </div>

    <p v-if="variances.length === 0" class="muted-text empty-budget-state">暂无预算偏差数据。</p>
    <div v-else class="variance-table-wrap">
      <table class="variance-table">
        <thead>
          <tr>
            <th scope="col">指标</th>
            <th scope="col">实际值</th>
            <th scope="col">预算值</th>
            <th scope="col">偏差金额</th>
            <th scope="col">偏差率</th>
            <th scope="col">状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="variance in variances" :key="variance.metricCode">
            <th scope="row">
              <span>{{ variance.metricName }}</span>
              <small>{{ variance.metricCode }}</small>
            </th>
            <td>{{ formatAmount(variance.actualValue, variance.unit) }}</td>
            <td>{{ formatAmount(variance.budgetValue, variance.unit) }}</td>
            <td>{{ formatAmount(variance.varianceAmount, variance.unit) }}</td>
            <td>
              <span class="variance-rate" :data-direction="variance.varianceRate > 0 ? 'up' : 'down'">
                {{ formatRate(variance.varianceRate) }}
              </span>
            </td>
            <td>
              <span class="variance-status" :data-direction="variance.varianceAmount > 0 ? 'over' : 'under'">
                {{ varianceLabel(variance.varianceAmount) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
