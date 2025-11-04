package de.jmale.core.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
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
import io.opentelemetry.api.common.AttributeKey;

import java.time.Duration;

/**
 * OpenTelemetry configuration for jMaLe application.
 * Provides centralized setup for tracing, metrics, and logging.
 * 
 * @author OpenTelemetry Instrumentation
 * @version 1.0.0
 * @since 1.0
 */
public class OpenTelemetryConfig {
    
    private static final String SERVICE_NAME = "jmale";
    private static final String SERVICE_VERSION = "1.0-SNAPSHOT";
    
    private static OpenTelemetry openTelemetry;
    private static Tracer tracer;
    
    /**
     * Initialize OpenTelemetry with default configuration.
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
                AttributeKey.stringKey("service.name"), SERVICE_NAME,
                AttributeKey.stringKey("service.version"), SERVICE_VERSION
            )));
        
        // Configure tracing
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(createSpanProcessor())
            .setResource(resource)
            .build();
        
        // Configure metrics
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(createMetricReader())
            .setResource(resource)
            .build();
        
        // Configure logging
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(createLogProcessor())
            .setResource(resource)
            .build();
        
        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            tracerProvider.close();
            meterProvider.close();
            loggerProvider.close();
        }));
        
        return openTelemetry;
    }
    
    /**
     * Get the configured OpenTelemetry instance.
     * 
     * @return OpenTelemetry instance
     */
    public static OpenTelemetry getOpenTelemetry() {
        if (openTelemetry == null) {
            return initialize();
        }
        return openTelemetry;
    }
    
    /**
     * Get the tracer for jMaLe application.
     * 
     * @return Tracer instance
     */
    public static Tracer getTracer() {
        if (tracer == null) {
            tracer = getOpenTelemetry().getTracer(SERVICE_NAME, SERVICE_VERSION);
        }
        return tracer;
    }
    
    private static BatchSpanProcessor createSpanProcessor() {
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            return BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build())
                .build();
        } else {
            // Fallback to logging exporter for development
            return BatchSpanProcessor.builder(LoggingSpanExporter.create())
                .build();
        }
    }
    
    private static PeriodicMetricReader createMetricReader() {
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            return PeriodicMetricReader.builder(
                OtlpGrpcMetricExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build())
                .setInterval(Duration.ofSeconds(30))
                .build();
        } else {
            // Fallback to logging exporter for development
            return PeriodicMetricReader.builder(LoggingMetricExporter.create())
                .setInterval(Duration.ofSeconds(30))
                .build();
        }
    }
    
    private static BatchLogRecordProcessor createLogProcessor() {
        String otlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
        
        if (otlpEndpoint != null && !otlpEndpoint.isEmpty()) {
            return BatchLogRecordProcessor.builder(
                OtlpGrpcLogRecordExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build())
                .build();
        } else {
            // For development, logs will be handled by logback configuration
            return BatchLogRecordProcessor.builder(
                OtlpGrpcLogRecordExporter.builder()
                    .setEndpoint("http://localhost:4317")
                    .build())
                .build();
        }
    }
}
