using Azure.Messaging.ServiceBus;
using Microsoft.AspNetCore.Mvc;
using System.Text.Json;

namespace JobManager.Api.Controllers;

[ApiController]
[Route("api/v1/jobs")]
public class JobsController : ControllerBase
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    private readonly ServiceBusClient _serviceBusClient;
    private readonly ILogger<JobsController> _logger;

    public JobsController(ServiceBusClient serviceBusClient, ILogger<JobsController> logger)
    {
        _serviceBusClient = serviceBusClient;
        _logger = logger;
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateJobRequest request)
    {
        var firstJobId = string.IsNullOrWhiteSpace(request.JobId) ? Guid.NewGuid().ToString("N") : request.JobId;
        var correlationId = string.IsNullOrWhiteSpace(request.CorrelationId) ? Guid.NewGuid().ToString("N") : request.CorrelationId;

        var jobIds = new List<string>(capacity: 10) { firstJobId };
        for (var index = 1; index < 1; index++)
        {
            jobIds.Add(Guid.NewGuid().ToString("N"));
        }

        await using var sender = _serviceBusClient.CreateSender(Env.GetRequiredConfig("JOB_CREATED_TOPIC"));

        foreach (var jobId in jobIds)
        {
            var payload = new JobCreatedEvent(jobId, correlationId, request.JobName, request.Amount, DateTimeOffset.UtcNow);

            var message = new ServiceBusMessage(BinaryData.FromObjectAsJson(payload, JsonOptions))
            {
                MessageId = jobId,
                CorrelationId = correlationId,
                Subject = "JobCreated",
                ContentType = "application/json"
            };

            await sender.SendMessageAsync(message);
            _logger.LogInformation("Published JobCreated for JobId={JobId}, CorrelationId={CorrelationId}", jobId, correlationId);
        }

        return Accepted($"/api/v1/jobs/{firstJobId}", new CreateJobResponse(firstJobId, correlationId, "JOB_CREATED_PUBLISHED"));
    }
}

public sealed record CreateJobRequest(string? JobId, string? CorrelationId, string JobName, decimal Amount);
public sealed record CreateJobResponse(string JobId, string CorrelationId, string Status);
public sealed record JobCreatedEvent(string JobId, string CorrelationId, string JobName, decimal Amount, DateTimeOffset CreatedAt);

internal static class Env
{
    public static string GetRequiredConfig(string key)
    {
        var value = Environment.GetEnvironmentVariable(key);
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new InvalidOperationException($"Missing required environment variable: {key}");
        }

        return value;
    }
}
