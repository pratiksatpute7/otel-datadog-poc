package com.example.demo.messaging;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.example.demo.service.WorkflowService;
import com.example.demo.workflow.dto.JobProcessedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class JobProcessedConsumer {

    private final ServiceBusProcessorClient processorClient;
    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper;

    public JobProcessedConsumer(
        ServiceBusClientBuilder clientBuilder,
        WorkflowService workflowService,
        ObjectMapper objectMapper,
        @Value("${azure.servicebus.topic-job-processed}") String topicName,
        @Value("${azure.servicebus.subscription-job-processed}") String subscriptionName
    ) {
        this.workflowService = workflowService;
        this.objectMapper = objectMapper;
        this.processorClient = clientBuilder
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .processMessage(context -> {
                try {
                    JobProcessedEvent event = objectMapper.readValue(context.getMessage().getBody().toBytes(), JobProcessedEvent.class);
                    workflowService.handleJobProcessed(event);
                    log.info("Consumed job-processed for jobId={}, correlationId={}", event.jobId(), event.correlationId());
                    context.complete();
                } catch (Exception ex) {
                    log.error("Failed processing job-processed message", ex);
                    context.abandon();
                }
            })
            .processError(this::processError)
            .disableAutoComplete()
            .buildProcessorClient();
    }

    @PostConstruct
    public void start() {
        processorClient.start();
        log.info("Started Service Bus consumer for topic job-processed");
    }

    @PreDestroy
    public void stop() {
        processorClient.close();
        log.info("Stopped Service Bus consumer for topic job-processed");
    }

    private void processError(ServiceBusErrorContext context) {
        if (context.getErrorSource() == ServiceBusErrorSource.USER_CALLBACK) {
            log.error("Service Bus callback error", context.getException());
            return;
        }
        log.error("Service Bus processor error on entity {}", context.getEntityPath(), context.getException());
    }
}
