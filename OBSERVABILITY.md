# jMaLe Observability Setup

This document describes the OpenTelemetry observability instrumentation added to the jMaLe project.

## Overview

The jMaLe project now includes comprehensive observability using OpenTelemetry, providing:

- **Distributed Tracing** - Track operations across the application
- **Metrics Collection** - Monitor application performance and health
- **Structured Logging** - Centralized logging with trace correlation
- **Health Monitoring** - Application health checks and status reporting

## Components

### ObservabilityConfig
Central configuration class that initializes OpenTelemetry SDK with:
- OTLP exporters for traces, metrics, and logs
- Fallback to console logging when OTLP endpoint is not configured
- Resource attributes for service identification
- Proper shutdown handling

### MetricsCollector
Utility class for collecting application metrics:
- Operation counters and duration histograms
- Error tracking and categorization
- Data processing metrics
- Custom business metrics

### HealthCheck
Simple health monitoring utility:
- System resource monitoring (memory, CPU)
- OpenTelemetry initialization status
- Extensible health check framework

### Structured Logging
Logback configuration with:
- OpenTelemetry appender for log correlation
- Structured log format with trace/span IDs
- Environment-specific log levels
- MDC (Mapped Diagnostic Context) support

## Configuration

### Environment Variables

Set the following environment variables to configure observability:

```bash
# OTLP Endpoint (required for remote telemetry)
export OTEL_EXPORTER_OTLP_ENDPOINT=http://your-otel-collector:4317

# Service identification
export OTEL_SERVICE_NAME=jmale
export OTEL_SERVICE_VERSION=1.0-SNAPSHOT

# Optional: Authentication headers
export OTEL_EXPORTER_OTLP_HEADERS="api-key=your-api-key"
```

### Local Development

For local development without an OTLP collector, the application will:
- Log traces and metrics to console
- Still provide full observability features
- Allow easy transition to remote collection

## Usage Examples

### Running with Observability

```bash
# Basic run (console output)
mvn exec:java -Dexec.mainClass="de.jmale.core.App" -pl jMaLe-core

# With OTLP endpoint
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
mvn exec:java -Dexec.mainClass="de.jmale.core.App" -pl jMaLe-core
```

### Health Check

The application includes a built-in health check that reports:
- Application status (HEALTHY/UNHEALTHY)
- Memory usage and system resources
- OpenTelemetry initialization status
- Uptime and performance metrics

### Custom Instrumentation

The observability framework is designed to be extended:

```java
// Add custom metrics
MetricsCollector.getInstance().recordOperation("custom.operation", durationMs);

// Add custom tracing
Span span = tracer.spanBuilder("custom.operation").startSpan();
try {
    // Your code here
} finally {
    span.end();
}
```

## Integration with Observe

This setup is compatible with Observe and other OpenTelemetry-compatible backends:

1. Set `OTEL_EXPORTER_OTLP_ENDPOINT` to your Observe endpoint
2. Configure authentication headers if required
3. The application will automatically send telemetry data

## Dependencies Added

- `io.opentelemetry:opentelemetry-api`
- `io.opentelemetry:opentelemetry-sdk`
- `io.opentelemetry:opentelemetry-exporter-otlp`
- `io.opentelemetry:opentelemetry-exporter-logging`
- `io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0`
- `ch.qos.logback:logback-classic`
- `io.opentelemetry.semconv:opentelemetry-semconv`

## Next Steps

This basic observability foundation can be extended with:
- Custom business metrics
- Additional tracing for specific operations
- Integration with monitoring dashboards
- Alerting based on health checks and metrics
- Performance profiling and optimization

The instrumentation is designed to be minimal and non-intrusive while providing comprehensive observability coverage.
