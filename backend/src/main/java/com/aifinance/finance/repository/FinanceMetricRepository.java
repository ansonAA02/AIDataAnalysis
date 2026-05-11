package com.aifinance.finance.repository;

import java.util.List;
import java.util.Optional;

import com.aifinance.finance.domain.FinanceMetric;
import com.aifinance.finance.domain.MetricCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceMetricRepository extends JpaRepository<FinanceMetric, Long> {

    Optional<FinanceMetric> findByPeriodIdAndMetricCode(Long periodId, MetricCode metricCode);

    List<FinanceMetric> findByPeriodIdOrderByMetricCodeAsc(Long periodId);
}
