# Use official Maven image with Java 17 for building
FROM maven:3.9.6-eclipse-temurin-17 as builder

# Set working directory
WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests && \
    ls -la target/

# Copy serverless-init from Datadog official image
FROM datadog/serverless-init:latest as serverless-init

# Final stage - minimal runtime image with JRE only
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy serverless-init from Datadog image
COPY --from=serverless-init /datadog-init /app/datadog-init

# Copy JAR from builder stage
COPY --from=builder /build/target/otel-datadog-poc-1.0.0.jar app.jar

# Download OpenTelemetry Java Agent (optional - serverless-init provides automatic instrumentation)
RUN apt-get update && apt-get install -y curl && \
    curl --max-time 300 -L -o opentelemetry-javaagent.jar \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.32.0/opentelemetry-javaagent.jar" && \
    ls -lah opentelemetry-javaagent.jar /app/datadog-init && \
    rm -rf /var/lib/apt/lists/*

# Expose port
EXPOSE 8080


# Use Datadog serverless-init as entrypoint with OpenTelemetry agent
ENTRYPOINT ["/app/datadog-init"]
CMD ["java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "app.jar"]
