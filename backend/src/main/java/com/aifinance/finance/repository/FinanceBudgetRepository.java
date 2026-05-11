package com.aifinance.finance.repository;

import java.util.List;
import java.util.Optional;

import com.aifinance.finance.domain.FinanceBudget;
import com.aifinance.finance.domain.MetricCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceBudgetRepository extends JpaRepository<FinanceBudget, Long> {

    Optional<FinanceBudget> findByPeriodIdAndMetricCode(Long periodId, MetricCode metricCode);

    List<FinanceBudget> findByPeriodIdOrderByMetricCodeAsc(Long periodId);
}
