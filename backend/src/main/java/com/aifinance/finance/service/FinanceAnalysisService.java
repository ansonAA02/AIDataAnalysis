package com.aifinance.finance.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.aifinance.finance.domain.FinanceMetric;
import com.aifinance.finance.domain.FinancePeriod;
import com.aifinance.finance.domain.MetricCode;
import com.aifinance.finance.domain.RiskLevel;
import com.aifinance.finance.domain.RiskType;
import com.aifinance.finance.dto.FinancialSnapshot;
import com.aifinance.finance.dto.RiskSignal;
import com.aifinance.finance.dto.VarianceResult;
import com.aifinance.finance.repository.FinanceBudgetRepository;
import com.aifinance.finance.repository.FinanceMetricRepository;
import com.aifinance.finance.repository.FinancePeriodRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class FinanceAnalysisService {

    private static final BigDecimal COST_GROWTH_FAST_THRESHOLD = new BigDecimal("10.00");
    private static final BigDecimal PROFIT_MARGIN_DROP_THRESHOLD = new BigDecimal("5.00");
    private static final BigDecimal BUDGET_OVERSPEND_THRESHOLD = new BigDecimal("5.00");

    private final FinancePeriodRepository financePeriodRepository;
    private final FinanceMetricRepository financeMetricRepository;
    private final FinanceBudgetRepository financeBudgetRepository;
    private final FinanceMetricCalculator calculator;

    public FinanceAnalysisService(
            FinancePeriodRepository financePeriodRepository,
            FinanceMetricRepository financeMetricRepository,
            FinanceBudgetRepository financeBudgetRepository,
            FinanceMetricCalculator calculator) {
        this.financePeriodRepository = financePeriodRepository;
        this.financeMetricRepository = financeMetricRepository;
        this.financeBudgetRepository = financeBudgetRepository;
        this.calculator = calculator;
    }

    public FinancialSnapshot snapshot(Long periodId) {
        Long requiredPeriodId = requirePeriodId(periodId);
        FinancePeriod period = financePeriodRepository.findById(requiredPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown finance period: " + requiredPeriodId));
        Map<MetricCode, BigDecimal> metrics = metricsByCode(requiredPeriodId);

        BigDecimal revenue = value(metrics, MetricCode.REVENUE);
        BigDecimal grossProfit = value(metrics, MetricCode.GROSS_PROFIT);
        BigDecimal netProfit = value(metrics, MetricCode.NET_PROFIT);

        return new FinancialSnapshot(
                period.getId(),
                period.getPeriodLabel(),
                revenue,
                value(metrics, MetricCode.COST),
                grossProfit,
                value(metrics, MetricCode.OPERATING_EXPENSE),
                netProfit,
                value(metrics, MetricCode.OPERATING_CASH_FLOW),
                calculator.percentage(grossProfit, revenue),
                calculator.percentage(netProfit, revenue),
                "万元");
    }

    public List<FinancialSnapshot> recentSnapshots(int months) {
        List<FinancePeriod> periods = financePeriodRepository.findAllByOrderByYearValueAscMonthValueAsc();
        int fromIndex = Math.max(0, periods.size() - months);

        return periods.subList(fromIndex, periods.size())
                .stream()
                .map(period -> snapshot(period.getId()))
                .toList();
    }

    public List<RiskSignal> risks(Long periodId) {
        Long requiredPeriodId = requirePeriodId(periodId);
        FinancialSnapshot current = snapshot(requiredPeriodId);
        FinancialSnapshot previous = previousSnapshot(requiredPeriodId);
        List<RiskSignal> risks = new ArrayList<>();

        if (current.operatingCashFlow().compareTo(BigDecimal.ZERO) < 0) {
            risks.add(new RiskSignal(
                    RiskType.CASH_FLOW_RISK,
                    RiskLevel.HIGH,
                    "经营现金流为负，短期偿付和回款压力上升。"));
        }

        if (previous != null) {
            BigDecimal costGrowth = calculator.percentage(
                    current.cost().subtract(previous.cost()),
                    previous.cost());
            BigDecimal revenueGrowth = calculator.percentage(
                    current.revenue().subtract(previous.revenue()),
                    previous.revenue());
            if (costGrowth.subtract(revenueGrowth).compareTo(COST_GROWTH_FAST_THRESHOLD) > 0) {
                risks.add(new RiskSignal(
                        RiskType.COST_GROWTH_FAST,
                        RiskLevel.HIGH,
                        "成本增速明显高于收入增速，需要排查成本异常增长。"));
            }

            BigDecimal marginDrop = previous.netMargin().subtract(current.netMargin());
            if (marginDrop.compareTo(PROFIT_MARGIN_DROP_THRESHOLD) >= 0) {
                risks.add(new RiskSignal(
                        RiskType.PROFIT_MARGIN_DROP,
                        RiskLevel.HIGH,
                        "净利率较上期明显下降，需要关注盈利质量。"));
            }

            if (current.revenue().compareTo(previous.revenue()) < 0) {
                risks.add(new RiskSignal(
                        RiskType.REVENUE_DECLINE,
                        RiskLevel.MEDIUM,
                        "收入较上期下降，需要复盘销售和客户转化。"));
            }
        }

        financeBudgetRepository.findByPeriodIdAndMetricCode(requiredPeriodId, MetricCode.COST)
                .ifPresent(budget -> {
                    VarianceResult variance = calculator.variance(current.cost(), budget.getBudgetValue());
                    if (variance.varianceRate().compareTo(BUDGET_OVERSPEND_THRESHOLD) > 0) {
                        risks.add(new RiskSignal(
                                RiskType.BUDGET_OVERSPEND,
                                RiskLevel.HIGH,
                                "成本实际值超过预算，需要复盘预算执行。"));
                    }
                });

        return risks;
    }

    public List<BudgetVariance> budgetVariances(Long periodId) {
        Long requiredPeriodId = requirePeriodId(periodId);
        Map<MetricCode, FinanceMetric> metrics = financeMetricsByCode(requiredPeriodId);

        return financeBudgetRepository.findByPeriodIdOrderByMetricCodeAsc(requiredPeriodId)
                .stream()
                .filter(budget -> metrics.containsKey(budget.getMetricCode()))
                .map(budget -> {
                    FinanceMetric metric = metrics.get(budget.getMetricCode());
                    VarianceResult variance = calculator.variance(metric.getActualValue(), budget.getBudgetValue());
                    return new BudgetVariance(
                            budget.getMetricCode(),
                            metric.getMetricName(),
                            variance.actualValue(),
                            variance.budgetValue(),
                            variance.varianceAmount(),
                            variance.varianceRate(),
                            budget.getUnit());
                })
                .toList();
    }

    private FinancialSnapshot previousSnapshot(Long periodId) {
        List<FinancePeriod> periods = financePeriodRepository.findAllByOrderByYearValueAscMonthValueAsc();
        periods.sort(Comparator.comparing(FinancePeriod::getYearValue).thenComparing(FinancePeriod::getMonthValue));

        for (int index = 0; index < periods.size(); index++) {
            if (periods.get(index).getId().equals(periodId) && index > 0) {
                return snapshot(periods.get(index - 1).getId());
            }
        }

        return null;
    }

    private Map<MetricCode, BigDecimal> metricsByCode(Long periodId) {
        Map<MetricCode, BigDecimal> metrics = new EnumMap<>(MetricCode.class);
        for (FinanceMetric metric : financeMetricRepository.findByPeriodIdOrderByMetricCodeAsc(periodId)) {
            metrics.put(metric.getMetricCode(), metric.getActualValue());
        }
        return metrics;
    }

    private Map<MetricCode, FinanceMetric> financeMetricsByCode(Long periodId) {
        Map<MetricCode, FinanceMetric> metrics = new EnumMap<>(MetricCode.class);
        for (FinanceMetric metric : financeMetricRepository.findByPeriodIdOrderByMetricCodeAsc(periodId)) {
            metrics.put(metric.getMetricCode(), metric);
        }
        return metrics;
    }

    private BigDecimal value(Map<MetricCode, BigDecimal> metrics, MetricCode code) {
        return metrics.getOrDefault(code, BigDecimal.ZERO);
    }

    private @NonNull Long requirePeriodId(Long periodId) {
        return Objects.requireNonNull(periodId, "periodId must not be null");
    }

    public record BudgetVariance(
            MetricCode metricCode,
            String metricName,
            BigDecimal actualValue,
            BigDecimal budgetValue,
            BigDecimal varianceAmount,
            BigDecimal varianceRate,
            String unit) {
    }
}
