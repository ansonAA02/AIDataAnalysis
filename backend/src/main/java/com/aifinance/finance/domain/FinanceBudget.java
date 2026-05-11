package com.aifinance.finance.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "finance_budget")
public class FinanceBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_code", nullable = false)
    private MetricCode metricCode;

    @Column(name = "budget_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal budgetValue;

    @Column(name = "unit", nullable = false)
    private String unit;

    protected FinanceBudget() {
    }

    public Long getId() {
        return id;
    }

    public Long getPeriodId() {
        return periodId;
    }

    public MetricCode getMetricCode() {
        return metricCode;
    }

    public BigDecimal getBudgetValue() {
        return budgetValue;
    }

    public String getUnit() {
        return unit;
    }
}
