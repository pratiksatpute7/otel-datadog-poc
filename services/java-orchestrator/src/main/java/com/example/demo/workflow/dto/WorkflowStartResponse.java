package com.example.demo.workflow.dto;

public record WorkflowStartResponse(String jobId, String correlationId, String status) {
}
