# jMaLe Observability Guide

This document describes the OpenTelemetry observability instrumentation added to the jMaLe (Java Machine Learning) project.

## Features

The observability implementation provides:

1. **Distributed Tracing** - Track request flows through the application
2. **Metrics Collection** - Monitor performance and resource usage
3. **Structured Logging** - Correlated logs with trace context
4. **Health Checks** - Monitor application status
5. **Error Tracking** - Capture and trace exceptions

## Configuration

### Environment Variables

Set these environment variables to configure observability:

```bash
# OTLP Endpoint (required for production)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://your-observe-endpoint:443

# Authentication headers for Observe
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer your-token"

# Service identification
export OTEL_SERVICE_NAME=jmale
export OTEL_SERVICE_VERSION=1.0-SNAPSHOT

# Resource attributes
export OTEL_RESOURCE_ATTRIBUTES="service.name=jmale,service.version=1.0-SNAPSHOT,deployment.environment=production"
```

### Observe Integration

For Observe integration, copy `observe-config-example.env` to `.env` and update with your credentials:

```bash
cp observe-config-example.env .env
# Edit .env with your actual Observe endpoint and token
```

## Usage

### Initialization

OpenTelemetry is automatically initialized when the application starts:

```java
// In your main method
OpenTelemetryConfig.initialize();
```

### Tracing

Use the TracingUtils class to add tracing to your methods:

```java
// Trace a method execution
TracingUtils.trace("operation_name", () -> {
    // Your code here
    return result;
});

// Add attributes to current span
TracingUtils.addAttribute("key", "value");
TracingUtils.addAttribute("count", 42);

// Record exceptions
try {
    // risky operation
} catch (Exception e) {
    TracingUtils.recordException(e);
    throw e;
}
```

### Metrics

Record custom metrics using MetricsCollector:

```java
MetricsCollector metrics = MetricsCollector.getInstance();

// Record operation success
metrics.recordSuccess("file_import", durationSeconds);

// Record operation failure
metrics.recordError("file_import", durationSeconds);

// Record file import
metrics.recordFileImport("csv", durationSeconds, "success");

// Record data processing
metrics.recordDataProcessing("clustering", "success");
```

### Health Checks

Perform health checks to monitor application status:

```java
HealthCheck.HealthResult result = HealthCheck.checkHealth();
if (result.isHealthy()) {
    logger.info("Application is healthy");
} else {
    logger.warn("Application has issues: {}", result.getMessage());
}
```

### Logging

Structured logging is automatically configured with trace correlation:

```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

// Logs will include trace_id and span_id for correlation
logger.info("Processing file: {}", filename);
logger.error("Error processing file", exception);
```

## Metrics Available

The following metrics are automatically collected:

- `jmale_operations_total` - Total number of operations
- `jmale_operation_duration_seconds` - Operation duration histogram
- `jmale_file_imports_total` - File import operations count
- `jmale_file_import_duration_seconds` - File import duration histogram
- `jmale_data_processing_total` - Data processing operations count

## Development

For development, observability data is logged to the console. Set these environment variables for debug output:

```bash
export DEBUG_LOGGING=true
export DEBUG_METRICS=true
export DEBUG_TRACES=true
```

## Production Deployment

1. Set the OTLP endpoint to your observability backend
2. Configure authentication headers
3. Set appropriate resource attributes
4. Ensure log directory permissions for file logging
5. Monitor the health check endpoint

## Troubleshooting

- Check logs for OpenTelemetry initialization messages
- Verify OTLP endpoint connectivity
- Ensure authentication credentials are correct
- Monitor resource usage and adjust sampling rates if needed
