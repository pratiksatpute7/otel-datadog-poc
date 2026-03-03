using System.Diagnostics;
using System.Text.Json;
using Azure.Messaging.ServiceBus;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;


IHost appHost = Host.CreateDefaultBuilder(args)
    .ConfigureWebHostDefaults(webBuilder =>
    {
        webBuilder.Configure(app =>
        {
            app.UseRouting();
            app.UseEndpoints(endpoints =>
            {
                endpoints.MapGet("/health", async context =>
                {
                    context.Response.ContentType = "application/json";
                    await context.Response.WriteAsync("{\"status\":\"UP\"}");
                });
            });
        });
    })
    .ConfigureServices(services =>
    {
        services.AddOpenTelemetry()
            .ConfigureResource(resource => resource.AddService("dps-worker"))
            .WithTracing(tracing => tracing
                .AddAspNetCoreInstrumentation()
                .AddHttpClientInstrumentation()
                .AddSource("Azure.Messaging.*")
                .AddOtlpExporter());

        services.AddSingleton(_ => new ServiceBusClient(GetRequiredConfig("SERVICEBUS_CONNECTION_STRING")));
        services.AddHostedService<DpsWorker>();
    })
    .Build();

await appHost.RunAsync();

static string GetRequiredConfig(string key)
{
    return Env.GetRequiredConfig(key);
}

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

    public static int GetPositiveIntOrDefault(string key, int defaultValue)
    {
        var value = Environment.GetEnvironmentVariable(key);
        if (int.TryParse(value, out var parsed) && parsed > 0)
        {
            return parsed;
        }

        return defaultValue;
    }
}

internal sealed class DpsWorker : BackgroundService
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        PropertyNameCaseInsensitive = true
    };

    private readonly ILogger<DpsWorker> _logger;
    private readonly ServiceBusClient _serviceBusClient;
    private ServiceBusProcessor? _processor;

    public DpsWorker(ILogger<DpsWorker> logger, ServiceBusClient serviceBusClient)
    {
        _logger = logger;
        _serviceBusClient = serviceBusClient;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        var inputTopic = Env.GetRequiredConfig("JOB_CREATED_TOPIC");
        var inputSubscription = Env.GetRequiredConfig("JOB_CREATED_SUBSCRIPTION");

        _processor = _serviceBusClient.CreateProcessor(inputTopic, inputSubscription, new ServiceBusProcessorOptions
        {
            AutoCompleteMessages = false,
            MaxConcurrentCalls = Env.GetPositiveIntOrDefault("DPS_MAX_CONCURRENT_CALLS", 50)
        });

        _processor.ProcessMessageAsync += ProcessMessageAsync;
        _processor.ProcessErrorAsync += ProcessErrorAsync;

        await _processor.StartProcessingAsync(stoppingToken);
        _logger.LogInformation("DPS worker started listening on topic {Topic} / subscription {Subscription}", inputTopic, inputSubscription);

        while (!stoppingToken.IsCancellationRequested)
        {
            await Task.Delay(TimeSpan.FromSeconds(1), stoppingToken);
        }
    }

    public override async Task StopAsync(CancellationToken cancellationToken)
    {
        if (_processor is not null)
        {
            await _processor.StopProcessingAsync(cancellationToken);
            await _processor.DisposeAsync();
        }

        await base.StopAsync(cancellationToken);
    }

    private async Task ProcessMessageAsync(ProcessMessageEventArgs args)
    {
        var jobCreated = JsonSerializer.Deserialize<JobCreatedEvent>(args.Message.Body, JsonOptions);
        if (jobCreated is null)
        {
            _logger.LogWarning("Received invalid JobCreated payload; dead-lettering message {MessageId}", args.Message.MessageId);
            await args.DeadLetterMessageAsync(args.Message, "InvalidPayload", "Unable to deserialize JobCreated payload");
            return;
        }

        await Task.Delay(TimeSpan.FromMilliseconds(300));

        var processed = new JobProcessedEvent(
            jobCreated.JobId,
            jobCreated.CorrelationId,
            "PROCESSED",
            jobCreated.Amount,
            DateTimeOffset.UtcNow);

        var sender = _serviceBusClient.CreateSender(Env.GetRequiredConfig("JOB_PROCESSED_TOPIC"));

        var output = new ServiceBusMessage(BinaryData.FromObjectAsJson(processed, JsonOptions))
        {
            MessageId = processed.JobId,
            CorrelationId = processed.CorrelationId,
            Subject = "JobProcessed",
            ContentType = "application/json"
        };

        await sender.SendMessageAsync(output);
        await sender.DisposeAsync();

        await args.CompleteMessageAsync(args.Message);

        _logger.LogInformation("Processed job {JobId} and published JobProcessed", processed.JobId);
    }

    private Task ProcessErrorAsync(ProcessErrorEventArgs args)
    {
        _logger.LogError(args.Exception, "Service Bus error. Entity={EntityPath}, Namespace={Namespace}", args.EntityPath, args.FullyQualifiedNamespace);
        return Task.CompletedTask;
    }
}

internal sealed record JobCreatedEvent(string JobId, string CorrelationId, string JobName, decimal Amount, DateTimeOffset CreatedAt);
internal sealed record JobProcessedEvent(string JobId, string CorrelationId, string Status, decimal Amount, DateTimeOffset ProcessedAt);
