package com.aifinance.dataimport.dto;

// Wire response for the CSV commit endpoint.
public record ImportCommitResponse(
        int processedRows,
        int insertedRows,
        int updatedRows,
        int skippedRows) {
}
