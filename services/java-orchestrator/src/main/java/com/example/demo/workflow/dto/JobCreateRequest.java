package com.example.demo.workflow.dto;

public record JobCreateRequest(String jobId, String correlationId, String jobName, double amount) {
}
