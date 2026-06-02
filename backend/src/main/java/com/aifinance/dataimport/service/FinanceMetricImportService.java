package com.aifinance.dataimport.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.aifinance.dataimport.dto.ImportCommitResponse;
import com.aifinance.dataimport.dto.ImportPreviewResponse;
import com.aifinance.dataimport.dto.ImportPreviewRow;
import com.aifinance.finance.domain.FinanceMetric;
import com.aifinance.finance.domain.FinancePeriod;
import com.aifinance.finance.domain.MetricCode;
import com.aifinance.finance.repository.FinanceMetricRepository;
import com.aifinance.finance.repository.FinancePeriodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// Service that parses a CSV upload of finance metrics, returns a preview, and
// upserts the rows into finance_metric on commit.
@Service
public class FinanceMetricImportService {

    // Required CSV header columns (case-sensitive, exact order not required).
    private static final List<String> REQUIRED_HEADERS = List.of(
            "periodLabel", "metricCode", "metricName", "metricCategory", "actualValue", "unit");

    private final FinancePeriodRepository periodRepository;
    private final FinanceMetricRepository metricRepository;

    public FinanceMetricImportService(
            FinancePeriodRepository periodRepository,
            FinanceMetricRepository metricRepository) {
        this.periodRepository = periodRepository;
        this.metricRepository = metricRepository;
    }

    // Parse a CSV upload and return per-row validation results without persisting.
    public ImportPreviewResponse preview(MultipartFile file) {
        List<String[]> lines = readCsv(file);
        if (lines.isEmpty()) {
            return new ImportPreviewResponse(0, 0, 0, List.of(), List.of());
        }
        String[] header = lines.get(0);
        List<String> headers = Arrays.asList(header);
        Map<String, Integer> idx = headerIndex(header);
        List<ImportPreviewRow> rows = new ArrayList<>();
        int valid = 0;
        int invalid = 0;
        // Cache periods loaded by label to avoid hitting the DB per row.
        Map<String, FinancePeriod> periodCache = new HashMap<>();

        for (int i = 1; i < lines.size(); i++) {
            String[] cells = lines.get(i);
            ImportPreviewRow row = buildPreviewRow(i + 1, cells, idx, periodCache);
            rows.add(row);
            if (row.valid()) {
                valid++;
            } else {
                invalid++;
            }
        }
        return new ImportPreviewResponse(rows.size(), valid, invalid, headers, rows);
    }

    // Re-parse the file and persist each valid row as an upsert (insert-or-update).
    @Transactional
    public ImportCommitResponse commit(MultipartFile file) {
        ImportPreviewResponse preview = preview(file);
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        for (ImportPreviewRow row : preview.rows()) {
            if (!row.valid() || row.periodId() == null) {
                skipped++;
                continue;
            }
            MetricCode code = MetricCode.valueOf(row.metricCode());
            Optional<FinanceMetric> existing = metricRepository
                    .findByPeriodIdAndMetricCode(row.periodId(), code);
            if (existing.isPresent()) {
                FinanceMetric metric = existing.get();
                metric.setActualValue(row.actualValue());
                metric.setUnit(row.unit());
                metricRepository.save(metric);
                updated++;
            } else {
                FinanceMetric metric = new FinanceMetric(
                        row.periodId(),
                        code,
                        row.metricName(),
                        row.metricCategory(),
                        row.actualValue(),
                        row.unit());
                metricRepository.save(metric);
                inserted++;
            }
        }
        return new ImportCommitResponse(preview.rows().size(), inserted, updated, skipped);
    }

    // Validate a single CSV row and turn it into a preview entry.
    private ImportPreviewRow buildPreviewRow(
            int rowNumber,
            String[] cells,
            Map<String, Integer> idx,
            Map<String, FinancePeriod> periodCache) {
        try {
            String periodLabel = cell(cells, idx, "periodLabel");
            String metricCode = cell(cells, idx, "metricCode");
            String metricName = cell(cells, idx, "metricName");
            String metricCategory = cell(cells, idx, "metricCategory");
            String actualValueRaw = cell(cells, idx, "actualValue");
            String unit = cell(cells, idx, "unit");

            if (periodLabel.isBlank() || metricCode.isBlank() || metricName.isBlank()
                    || actualValueRaw.isBlank()) {
                return invalidRow(rowNumber, "缺少必填字段");
            }

            // Parse + validate metric code against the enum.
            MetricCode code;
            try {
                code = MetricCode.valueOf(metricCode);
            } catch (IllegalArgumentException ex) {
                return invalidRow(rowNumber, "未知 metricCode: " + metricCode);
            }

            // Parse the numeric actual value with helpful error messaging.
            BigDecimal actualValue;
            try {
                actualValue = new BigDecimal(actualValueRaw.trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                return invalidRow(rowNumber, "actualValue 不是合法数字");
            }

            // Resolve the period by label so users can use 2024-Q1 etc.
            FinancePeriod period = periodCache.computeIfAbsent(periodLabel,
                    label -> periodRepository.findAll().stream()
                            .filter(p -> p.getPeriodLabel().equalsIgnoreCase(label))
                            .findFirst()
                            .orElse(null));
            if (period == null) {
                return new ImportPreviewRow(rowNumber, null, periodLabel, metricCode,
                        metricName, metricCategory, actualValue, unit, false,
                        "找不到期间: " + periodLabel);
            }

            return new ImportPreviewRow(rowNumber, period.getId(), periodLabel, metricCode,
                    metricName, metricCategory, actualValue, unit, true, null);
        } catch (RuntimeException ex) {
            return invalidRow(rowNumber, "解析失败: " + ex.getMessage());
        }
    }

    // Build an invalid preview row with all-null data plus an error message.
    private ImportPreviewRow invalidRow(int rowNumber, String error) {
        return new ImportPreviewRow(rowNumber, null, null, null, null, null, null, null, false, error);
    }

    // Read all CSV lines into a list of trimmed cell arrays.
    private List<String[]> readCsv(MultipartFile file) {
        List<String[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    // Strip UTF-8 BOM that some editors (e.g. PowerShell Out-File) prepend.
                    line = line.substring(1);
                }
                firstLine = false;
                if (line.isBlank()) {
                    continue;
                }
                lines.add(splitCsvLine(line));
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("CSV 读取失败: " + ex.getMessage(), ex);
        }
        if (!lines.isEmpty()) {
            ensureRequiredHeaders(lines.get(0));
        }
        return lines;
    }

    // Throw a clear error if a required header column is missing.
    private void ensureRequiredHeaders(String[] header) {
        List<String> headerList = Arrays.stream(header).map(String::trim).toList();
        for (String required : REQUIRED_HEADERS) {
            if (!headerList.contains(required)) {
                throw new IllegalArgumentException("CSV 缺少必填表头: " + required);
            }
        }
    }

    // Build a header-name -> column-index map for cell lookups.
    private Map<String, Integer> headerIndex(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i].trim(), i);
        }
        return map;
    }

    // Lookup a cell by header name, returning empty string when absent.
    private String cell(String[] cells, Map<String, Integer> idx, String name) {
        Integer index = idx.get(name);
        if (index == null || index >= cells.length) {
            return "";
        }
        return cells[index] == null ? "" : cells[index].trim();
    }

    // Minimal CSV splitter: supports double-quoted fields with embedded commas.
    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                fields.add(buffer.toString());
                buffer.setLength(0);
                continue;
            }
            buffer.append(c);
        }
        fields.add(buffer.toString());
        return fields.toArray(new String[0]);
    }
}
