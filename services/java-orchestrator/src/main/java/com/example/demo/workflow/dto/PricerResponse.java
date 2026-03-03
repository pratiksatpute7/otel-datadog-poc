package com.example.demo.workflow.dto;

import java.time.OffsetDateTime;

public record PricerResponse(
    String jobId,
    String correlationId,
    double baseAmount,
    double finalPrice,
    String currency,
    OffsetDateTime calculatedAt
) {
}
