package de.jmale.core.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

/**
 * Centralized metrics collection for jMaLe application.
 * Provides common metrics for operations, errors, and performance.
 * 
 * @author OpenTelemetry Integration
 * @version 1.0.0
 * @since 1.0.0
 */
public class MetricsCollector {
    
    private static final String METER_NAME = "jmale.metrics";
    private static final String METER_VERSION = "1.0.0";
    
    // Attribute keys
    private static final AttributeKey<String> OPERATION_KEY = AttributeKey.stringKey("operation");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");
    private static final AttributeKey<String> ERROR_TYPE_KEY = AttributeKey.stringKey("error.type");
    
    private final Meter meter;
    private final LongCounter operationCounter;
    private final LongCounter errorCounter;
    private final DoubleHistogram operationDuration;
    private final LongCounter dataProcessedCounter;
    
    private static MetricsCollector instance;
    
    private MetricsCollector(OpenTelemetry openTelemetry) {
        this.meter = openTelemetry.getMeter(METER_NAME);
        
        this.operationCounter = meter
            .counterBuilder("jmale.operations.total")
            .setDescription("Total number of operations performed")
            .build();
            
        this.errorCounter = meter
            .counterBuilder("jmale.errors.total")
            .setDescription("Total number of errors encountered")
            .build();
            
        this.operationDuration = meter
            .histogramBuilder("jmale.operation.duration")
            .setDescription("Duration of operations in milliseconds")
            .setUnit("ms")
            .build();
            
        this.dataProcessedCounter = meter
            .counterBuilder("jmale.data.processed.total")
            .setDescription("Total number of data instances processed")
            .build();
    }
    
    /**
     * Initialize the metrics collector with OpenTelemetry instance.
     * 
     * @param openTelemetry configured OpenTelemetry instance
     * @return MetricsCollector instance
     */
    public static synchronized MetricsCollector initialize(OpenTelemetry openTelemetry) {
        if (instance == null) {
            instance = new MetricsCollector(openTelemetry);
        }
        return instance;
    }
    
    /**
     * Get the metrics collector instance.
     * 
     * @return MetricsCollector instance
     * @throws IllegalStateException if not initialized
     */
    public static MetricsCollector getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MetricsCollector not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Record a successful operation.
     * 
     * @param operationName name of the operation
     * @param durationMs duration in milliseconds
     */
    public void recordOperation(String operationName, double durationMs) {
        Attributes attributes = Attributes.of(
            OPERATION_KEY, operationName,
            STATUS_KEY, "success"
        );
        
        operationCounter.add(1, attributes);
        operationDuration.record(durationMs, attributes);
    }
    
    /**
     * Record a failed operation.
     * 
     * @param operationName name of the operation
     * @param durationMs duration in milliseconds
     * @param errorType type of error encountered
     */
    public void recordError(String operationName, double durationMs, String errorType) {
        Attributes operationAttributes = Attributes.of(
            OPERATION_KEY, operationName,
            STATUS_KEY, "error"
        );
        
        Attributes errorAttributes = Attributes.of(
            OPERATION_KEY, operationName,
            ERROR_TYPE_KEY, errorType
        );
        
        operationCounter.add(1, operationAttributes);
        operationDuration.record(durationMs, operationAttributes);
        errorCounter.add(1, errorAttributes);
    }
    
    /**
     * Record data processing metrics.
     * 
     * @param operationName name of the data processing operation
     * @param count number of data instances processed
     */
    public void recordDataProcessed(String operationName, long count) {
        Attributes attributes = Attributes.of(OPERATION_KEY, operationName);
        dataProcessedCounter.add(count, attributes);
    }
    
    /**
     * Record a simple counter increment.
     * 
     * @param operationName name of the operation
     */
    public void incrementOperation(String operationName) {
        Attributes attributes = Attributes.of(
            OPERATION_KEY, operationName,
            STATUS_KEY, "success"
        );
        operationCounter.add(1, attributes);
    }
}
