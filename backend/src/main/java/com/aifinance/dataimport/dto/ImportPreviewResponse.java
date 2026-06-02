package com.aifinance.dataimport.dto;

import java.util.List;

// Wire response for the CSV preview endpoint.
public record ImportPreviewResponse(
        int totalRows,
        int validRows,
        int invalidRows,
        List<String> headers,
        List<ImportPreviewRow> rows) {
}
