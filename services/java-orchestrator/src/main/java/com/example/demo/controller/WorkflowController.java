package com.example.demo.controller;

import com.example.demo.service.WorkflowService;
import com.example.demo.workflow.dto.WorkflowStartRequest;
import com.example.demo.workflow.dto.WorkflowStartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/start")
    public ResponseEntity<WorkflowStartResponse> startWorkflow(@RequestBody WorkflowStartRequest request) {
        log.debug("POST request: start workflow for jobName={}", request.jobName());
        WorkflowStartResponse response = workflowService.startWorkflow(request);
        return ResponseEntity.accepted().body(response);
    }
}
