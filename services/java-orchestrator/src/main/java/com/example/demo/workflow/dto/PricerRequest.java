package com.example.demo.workflow.dto;

public record PricerRequest(String jobId, String correlationId, double amount) {
}
