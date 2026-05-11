package com.aifinance.finance.repository;

import java.util.List;

import com.aifinance.finance.domain.FinancePeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancePeriodRepository extends JpaRepository<FinancePeriod, Long> {

    List<FinancePeriod> findAllByOrderByYearValueAscMonthValueAsc();
}
