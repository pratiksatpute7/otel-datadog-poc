package com.example.demo.service;

import com.example.demo.workflow.dto.JobCreateAcceptedResponse;
import com.example.demo.workflow.dto.JobCreateRequest;
import com.example.demo.workflow.dto.JobProcessedEvent;
import com.example.demo.workflow.dto.PricerRequest;
import com.example.demo.workflow.dto.PricerResponse;
import com.example.demo.workflow.dto.WorkflowStartRequest;
import com.example.demo.workflow.dto.WorkflowStartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final RestTemplate restTemplate;

    @Value("${workflow.job-manager-url}")
    private String jobManagerUrl;

    @Value("${workflow.pricer-url}")
    private String pricerUrl;

    public WorkflowStartResponse startWorkflow(WorkflowStartRequest request) {
        String jobId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        JobCreateRequest jobCreateRequest = new JobCreateRequest(jobId, correlationId, request.jobName(), request.amount());
        ResponseEntity<JobCreateAcceptedResponse> response = restTemplate.postForEntity(
            jobManagerUrl + "/api/v1/jobs",
            jobCreateRequest,
            JobCreateAcceptedResponse.class
        );

        log.info("Java submitted job {} to job-manager and received HTTP {}", jobId, response.getStatusCode().value());

        return new WorkflowStartResponse(jobId, correlationId, "JOB_SUBMITTED");
    }

    public void handleJobProcessed(JobProcessedEvent event) {
        PricerRequest request = new PricerRequest(event.jobId(), event.correlationId(), event.amount());
        ResponseEntity<PricerResponse> response = restTemplate.postForEntity(
            pricerUrl + "/api/v1/pricer/quote",
            request,
            PricerResponse.class
        );

        log.info(
            "Pricer completed for jobId={}, correlationId={}, status={}",
            event.jobId(),
            event.correlationId(),
            response.getStatusCode().value()
        );
    }
}
