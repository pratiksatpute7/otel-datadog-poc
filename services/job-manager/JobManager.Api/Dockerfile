FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY . .
RUN dotnet publish JobManager.Api.csproj -c Release -o /app/publish

FROM datadog/serverless-init:latest AS serverless-init

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=serverless-init /datadog-init /app/datadog-init
COPY --from=build /app/publish .
EXPOSE 8081
ENTRYPOINT ["/app/datadog-init"]
CMD ["dotnet", "JobManager.Api.dll"]
