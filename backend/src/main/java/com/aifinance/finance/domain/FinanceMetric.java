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
@Table(name = "finance_metric")
public class FinanceMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_code", nullable = false)
    private MetricCode metricCode;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "metric_category", nullable = false)
    private String metricCategory;

    @Column(name = "actual_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal actualValue;

    @Column(name = "unit", nullable = false)
    private String unit;

    protected FinanceMetric() {
    }

    // Convenience constructor used by the CSV import service.
    public FinanceMetric(
            Long periodId,
            MetricCode metricCode,
            String metricName,
            String metricCategory,
            BigDecimal actualValue,
            String unit) {
        this.periodId = periodId;
        this.metricCode = metricCode;
        this.metricName = metricName;
        this.metricCategory = metricCategory;
        this.actualValue = actualValue;
        this.unit = unit;
    }

    // Setter exposed so the import service can upsert existing rows.
    public void setActualValue(BigDecimal actualValue) {
        this.actualValue = actualValue;
    }

    // Setter used when re-importing changes the human-readable unit.
    public void setUnit(String unit) {
        this.unit = unit;
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

    public String getMetricName() {
        return metricName;
    }

    public String getMetricCategory() {
        return metricCategory;
    }

    public BigDecimal getActualValue() {
        return actualValue;
    }

    public String getUnit() {
        return unit;
    }
}
