package com.aifinance.finance.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.aifinance.finance.domain.BusinessLineMetric;
import com.aifinance.finance.domain.FinanceBudget;
import com.aifinance.finance.domain.FinanceMetric;
import com.aifinance.finance.domain.FinancePeriod;
import com.aifinance.finance.domain.MetricCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FinanceRepositoryTest {

    @Autowired
    private FinancePeriodRepository financePeriodRepository;

    @Autowired
    private FinanceMetricRepository financeMetricRepository;

    @Autowired
    private FinanceBudgetRepository financeBudgetRepository;

    @Autowired
    private BusinessLineMetricRepository businessLineMetricRepository;

    @Test
    void repositoriesReadTwelveMonthsOfSeededFinanceData() {
        List<FinancePeriod> periods = financePeriodRepository.findAllByOrderByYearValueAscMonthValueAsc();

        assertThat(periods).hasSize(12);
        assertThat(periods.getFirst().getPeriodLabel()).isEqualTo("2025-06");
        assertThat(periods.getLast().getPeriodLabel()).isEqualTo("2026-05");

        FinancePeriod september = periods.get(3);
        FinanceMetric cost = financeMetricRepository
                .findByPeriodIdAndMetricCode(september.getId(), MetricCode.COST)
                .orElseThrow();
        FinanceBudget costBudget = financeBudgetRepository
                .findByPeriodIdAndMetricCode(september.getId(), MetricCode.COST)
                .orElseThrow();
        List<BusinessLineMetric> businessLines = businessLineMetricRepository
                .findByPeriodIdOrderByBusinessLineAsc(september.getId());

        assertThat(cost.getActualValue()).isEqualByComparingTo(new BigDecimal("910.00"));
        assertThat(costBudget.getBudgetValue()).isEqualByComparingTo(new BigDecimal("780.00"));
        assertThat(businessLines).hasSize(2);
        assertThat(MetricCode.values()).contains(
                MetricCode.REVENUE,
                MetricCode.COST,
                MetricCode.GROSS_PROFIT,
                MetricCode.NET_PROFIT,
                MetricCode.OPERATING_CASH_FLOW,
                MetricCode.OPERATING_EXPENSE);
    }
}
