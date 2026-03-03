using OpenTelemetry.Resources;
using OpenTelemetry.Trace;


var builder = WebApplication.CreateBuilder(args);

builder.Services.AddOpenTelemetry()
    .ConfigureResource(resource => resource.AddService("pricer"))
    .WithTracing(tracing => tracing
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddSource("Azure.Messaging.*")
        .AddOtlpExporter());

builder.Services.AddControllers();

var app = builder.Build();
app.MapControllers();

app.Run();
