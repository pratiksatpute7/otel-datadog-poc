using Microsoft.AspNetCore.Mvc;

namespace Pricer.Api.Controllers;

[ApiController]
[Route("api/v1/pricer")]
public class PricerController : ControllerBase
{
    [HttpPost("quote")]
    public IActionResult Quote([FromBody] PricerRequest request)
    {
        var multiplier = 1.18m;
        var price = Math.Round(request.Amount * multiplier, 2, MidpointRounding.AwayFromZero);

        return Ok(new PricerResponse(request.JobId, request.CorrelationId, request.Amount, price, "USD", DateTimeOffset.UtcNow));
    }
}

public sealed record PricerRequest(string JobId, string CorrelationId, decimal Amount);
public sealed record PricerResponse(string JobId, string CorrelationId, decimal BaseAmount, decimal FinalPrice, string Currency, DateTimeOffset CalculatedAt);
