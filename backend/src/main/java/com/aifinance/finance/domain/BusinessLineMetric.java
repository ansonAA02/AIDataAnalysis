package com.aifinance.finance.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "business_line_metric")
public class BusinessLineMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_id", nullable = false)
    private Long periodId;

    @Column(name = "business_line", nullable = false)
    private String businessLine;

    @Column(name = "revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal revenue;

    @Column(name = "cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal cost;

    @Column(name = "gross_profit", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossProfit;

    @Column(name = "net_profit", nullable = false, precision = 18, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "unit", nullable = false)
    private String unit;

    protected BusinessLineMetric() {
    }

    public Long getId() {
        return id;
    }

    public Long getPeriodId() {
        return periodId;
    }

    public String getBusinessLine() {
        return businessLine;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public String getUnit() {
        return unit;
    }
}
