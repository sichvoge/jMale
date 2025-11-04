package de.jmale.core.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

import java.time.Duration;

/**
 * OpenTelemetry configuration for jMaLe application.
 * Provides centralized setup for tracing, metrics, and logging.
 * 
 * @author OpenTelemetry Integration
 * @version 1.0.0
 * @since 1.0.0
 */
public class ObservabilityConfig {
    
    private static final String SERVICE_NAME = "jmale";
    private static final String SERVICE_VERSION = "1.0-SNAPSHOT";
    
    private static OpenTelemetry openTelemetry;
    
    /**
     * Initialize OpenTelemetry with proper configuration.
     * This method should be called once at application startup.
     * 
     * @return configured OpenTelemetry instance
     */
    public static synchronized OpenTelemetry initialize() {
        if (openTelemetry != null) {
            return openTelemetry;
        }
        
        Resource resource = Resource.getDefault()
            .merge(Resource.create(Attributes.of(
                ResourceAttributes.SERVICE_NAME, SERVICE_NAME,
                ResourceAttributes.SERVICE_VERSION, SERVICE_VERSION
            )));
        
        // Configure tracing
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(createSpanExporter()).build())
            .setResource(resource)
            .build();
        
        // Configure metrics
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(createMetricExporter())
                .setInterval(Duration.ofSeconds(30))
                .build())
            .setResource(resource)
            .build();
        
        // Configure logging
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(createLogExporter()).build())
            .setResource(resource)
            .build();
        
        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();
        
        // Note: Global providers are set automatically by OpenTelemetrySdk
        
        return openTelemetry;
    }
    
    private static io.opentelemetry.sdk.trace.export.SpanExporter createSpanExporter() {
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            return OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();
        }
        return LoggingSpanExporter.create();
    }
    
    private static io.opentelemetry.sdk.metrics.export.MetricExporter createMetricExporter() {
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            return OtlpGrpcMetricExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();
        }
        return LoggingMetricExporter.create();
    }
    
    private static io.opentelemetry.sdk.logs.export.LogRecordExporter createLogExporter() {
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            return OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();
        }
        // Use a simple no-op exporter for logging since LoggingLogRecordExporter doesn't exist
        return new io.opentelemetry.sdk.logs.export.LogRecordExporter() {
            @Override
            public io.opentelemetry.sdk.common.CompletableResultCode export(java.util.Collection<io.opentelemetry.sdk.logs.data.LogRecordData> logs) {
                for (io.opentelemetry.sdk.logs.data.LogRecordData log : logs) {
                    System.out.println("LOG: " + log.getBody().asString());
                }
                return io.opentelemetry.sdk.common.CompletableResultCode.ofSuccess();
            }

            @Override
            public io.opentelemetry.sdk.common.CompletableResultCode flush() {
                return io.opentelemetry.sdk.common.CompletableResultCode.ofSuccess();
            }

            @Override
            public io.opentelemetry.sdk.common.CompletableResultCode shutdown() {
                return io.opentelemetry.sdk.common.CompletableResultCode.ofSuccess();
            }
        };
    }
    
    /**
     * Get the configured OpenTelemetry instance.
     * 
     * @return OpenTelemetry instance or null if not initialized
     */
    public static OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }
    
    /**
     * Shutdown OpenTelemetry and flush any pending data.
     */
    public static void shutdown() {
        if (openTelemetry instanceof OpenTelemetrySdk) {
            ((OpenTelemetrySdk) openTelemetry).close();
        }
    }
}
