#!/bin/bash

# OpenTelemetry POC Startup Script
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================="
echo "OpenTelemetry Spring Boot POC"
echo "========================================="
echo ""
echo "Project Path: $PROJECT_DIR"
echo ""

# Check if Docker and Docker Compose are installed
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed or not in PATH"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "ERROR: Docker Compose is not installed or not in PATH"
    exit 1
fi

echo "✓ Docker and Docker Compose found"
echo ""

# Build and start services
echo "Starting services with Docker Compose..."
echo ""

cd "$PROJECT_DIR"

# Pull images
echo "Pulling Docker images..."
docker-compose pull

# Build the application
echo ""
echo "Building application (this may take a few minutes)..."
docker-compose build app

# Start services
echo ""
echo "Starting services..."
docker-compose up -d

# Wait for services to be healthy
echo ""
echo "Waiting for services to be ready..."
sleep 5

# Check health
echo ""
echo "Checking application health..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ Application is healthy!"
        break
    fi
    attempt=$((attempt + 1))
    echo "  Attempt $attempt/$max_attempts - waiting for app..."
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "✗ Application health check failed"
    echo ""
    echo "Check logs with:"
    echo "  docker-compose logs app"
    exit 1
fi

echo ""
echo "========================================="
echo "✓ POC is running successfully!"
echo "========================================="
echo ""
echo "Quick Links:"
echo "  API Base: http://localhost:8080"
echo "  Get Products: curl http://localhost:8080/api/v1/products"
echo "  Health: http://localhost:8080/actuator/health"
echo "  Metrics: http://localhost:8080/actuator/metrics"
echo ""
echo "View Logs:"
echo "  App: docker-compose logs -f app"
echo "  Agent: docker-compose logs -f datadog-agent"
echo ""
echo "Stop Services:"
echo "  docker-compose down"
echo ""
