package com.example.demo.workflow.dto;

import java.time.OffsetDateTime;

public record JobProcessedEvent(
    String jobId,
    String correlationId,
    String status,
    double amount,
    OffsetDateTime processedAt
) {
}
