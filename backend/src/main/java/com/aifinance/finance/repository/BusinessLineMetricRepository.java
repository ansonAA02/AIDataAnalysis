package com.aifinance.finance.repository;

import java.util.List;

import com.aifinance.finance.domain.BusinessLineMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessLineMetricRepository extends JpaRepository<BusinessLineMetric, Long> {

    List<BusinessLineMetric> findByPeriodIdOrderByBusinessLineAsc(Long periodId);
}
