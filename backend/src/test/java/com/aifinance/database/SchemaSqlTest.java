package com.aifinance.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

class SchemaSqlTest {

    @Test
    void schemaDefinesFinanceTablesAndMoneyColumns() throws Exception {
        ClassPathResource schema = new ClassPathResource("db/schema.sql");

        assertThat(schema.exists()).isTrue();

        String sql = StreamUtils.copyToString(schema.getInputStream(), StandardCharsets.UTF_8)
                .toLowerCase();

        assertThat(sql).contains("create table if not exists finance_period");
        assertThat(sql).contains("create table if not exists finance_metric");
        assertThat(sql).contains("create table if not exists finance_budget");
        assertThat(sql).contains("create table if not exists business_line_metric");
        assertThat(sql).contains("decimal(18,2)");
    }
}
