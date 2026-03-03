using System.Diagnostics;
using Azure.Messaging.ServiceBus;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;


var builder = WebApplication.CreateBuilder(args);

builder.Services.AddOpenTelemetry()
    .ConfigureResource(resource => resource.AddService("job-manager"))
    .WithTracing(tracing => tracing
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddSource("Azure.Messaging.*")
        .AddOtlpExporter());

builder.Services.AddSingleton(_ => new ServiceBusClient(GetRequiredConfig("SERVICEBUS_CONNECTION_STRING")));
builder.Services.AddControllers();

var app = builder.Build();
app.MapControllers();

app.Run();

static string GetRequiredConfig(string key)
{
    var value = Environment.GetEnvironmentVariable(key);
    if (string.IsNullOrWhiteSpace(value))
    {
        throw new InvalidOperationException($"Missing required environment variable: {key}");
    }

    return value;
}
