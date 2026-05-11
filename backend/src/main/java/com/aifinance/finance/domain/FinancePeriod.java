package com.aifinance.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "finance_period")
public class FinancePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_value", nullable = false)
    private Integer yearValue;

    @Column(name = "month_value", nullable = false)
    private Integer monthValue;

    @Column(name = "quarter_value", nullable = false)
    private Integer quarterValue;

    @Column(name = "period_label", nullable = false)
    private String periodLabel;

    protected FinancePeriod() {
    }

    public Long getId() {
        return id;
    }

    public Integer getYearValue() {
        return yearValue;
    }

    public Integer getMonthValue() {
        return monthValue;
    }

    public Integer getQuarterValue() {
        return quarterValue;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }
}
