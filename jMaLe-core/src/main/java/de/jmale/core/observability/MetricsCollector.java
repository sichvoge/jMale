package de.jmale.core.observability;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

/**
 * Metrics collector for jMaLe application performance monitoring.
 * Provides methods to record various application metrics.
 * 
 * @author OpenTelemetry Instrumentation
 * @version 1.0.0
 * @since 1.0
 */
public class MetricsCollector {
    
    private static final AttributeKey<String> OPERATION_KEY = AttributeKey.stringKey("operation");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");
    private static final AttributeKey<String> FILE_TYPE_KEY = AttributeKey.stringKey("file_type");
    
    private final Meter meter;
    private final LongCounter operationCounter;
    private final DoubleHistogram operationDuration;
    private final LongCounter fileImportCounter;
    private final DoubleHistogram fileImportDuration;
    private final LongCounter dataProcessingCounter;
    
    private static MetricsCollector instance;
    
    private MetricsCollector() {
        this.meter = OpenTelemetryConfig.getOpenTelemetry()
            .getMeter("jmale-metrics");
        
        this.operationCounter = meter
            .counterBuilder("jmale_operations_total")
            .setDescription("Total number of operations performed")
            .build();
        
        this.operationDuration = meter
            .histogramBuilder("jmale_operation_duration_seconds")
            .setDescription("Duration of operations in seconds")
            .setUnit("s")
            .build();
        
        this.fileImportCounter = meter
            .counterBuilder("jmale_file_imports_total")
            .setDescription("Total number of file import operations")
            .build();
        
        this.fileImportDuration = meter
            .histogramBuilder("jmale_file_import_duration_seconds")
            .setDescription("Duration of file import operations in seconds")
            .setUnit("s")
            .build();
        
        this.dataProcessingCounter = meter
            .counterBuilder("jmale_data_processing_total")
            .setDescription("Total number of data processing operations")
            .build();
    }
    
    /**
     * Get the singleton instance of MetricsCollector.
     * 
     * @return MetricsCollector instance
     */
    public static synchronized MetricsCollector getInstance() {
        if (instance == null) {
            instance = new MetricsCollector();
        }
        return instance;
    }
    
    /**
     * Record a generic operation with duration.
     * 
     * @param operation the operation name
     * @param durationSeconds the duration in seconds
     * @param status the operation status (success, error, etc.)
     */
    public void recordOperation(String operation, double durationSeconds, String status) {
        Attributes attributes = Attributes.of(
            OPERATION_KEY, operation,
            STATUS_KEY, status
        );
        
        operationCounter.add(1, attributes);
        operationDuration.record(durationSeconds, attributes);
    }
    
    /**
     * Record a file import operation.
     * 
     * @param fileType the type of file being imported (csv, etc.)
     * @param durationSeconds the duration in seconds
     * @param status the operation status
     */
    public void recordFileImport(String fileType, double durationSeconds, String status) {
        Attributes attributes = Attributes.of(
            FILE_TYPE_KEY, fileType,
            STATUS_KEY, status
        );
        
        fileImportCounter.add(1, attributes);
        fileImportDuration.record(durationSeconds, attributes);
    }
    
    /**
     * Record a data processing operation.
     * 
     * @param operation the processing operation name
     * @param status the operation status
     */
    public void recordDataProcessing(String operation, String status) {
        Attributes attributes = Attributes.of(
            OPERATION_KEY, operation,
            STATUS_KEY, status
        );
        
        dataProcessingCounter.add(1, attributes);
    }
    
    /**
     * Record a successful operation.
     * 
     * @param operation the operation name
     * @param durationSeconds the duration in seconds
     */
    public void recordSuccess(String operation, double durationSeconds) {
        recordOperation(operation, durationSeconds, "success");
    }
    
    /**
     * Record a failed operation.
     * 
     * @param operation the operation name
     * @param durationSeconds the duration in seconds
     */
    public void recordError(String operation, double durationSeconds) {
        recordOperation(operation, durationSeconds, "error");
    }
}
