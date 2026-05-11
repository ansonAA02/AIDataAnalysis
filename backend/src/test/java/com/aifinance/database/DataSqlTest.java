package com.aifinance.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

class DataSqlTest {

    @Test
    void dataSqlDefinesTwelveMonthsAndRequiredBusinessEvents() throws Exception {
        ClassPathResource dataSql = new ClassPathResource("db/data.sql");

        assertThat(dataSql.exists()).isTrue();

        String sql = StreamUtils.copyToString(dataSql.getInputStream(), StandardCharsets.UTF_8)
                .toLowerCase();

        assertThat(sql).contains("cost_growth_fast");
        assertThat(sql).contains("cash_flow_risk");
        assertThat(sql).contains("budget_overspend");
        assertThat(sql).contains("profit_margin_drop");
        assertThat(sql).contains("operating_cash_flow");
        assertThat(sql).contains("business_line_metric");
        assertThat(sql.split("finance_period").length - 1).isGreaterThanOrEqualTo(1);
    }
}
