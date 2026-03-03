using Microsoft.AspNetCore.Mvc;

namespace Pricer.Api.Controllers;

[ApiController]
public class HealthController : ControllerBase
{
    [HttpGet("/health")]
    public IActionResult Get() => Ok(new { status = "UP" });
}
