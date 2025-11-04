package de.jmale.core.observability;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check utility for jMaLe application.
 * Provides basic health status information.
 * 
 * @author OpenTelemetry Integration
 * @version 1.0.0
 * @since 1.0.0
 */
public class HealthCheck {
    
    public enum Status {
        HEALTHY,
        UNHEALTHY,
        UNKNOWN
    }
    
    private static final long startTime = System.currentTimeMillis();
    
    /**
     * Perform a basic health check of the application.
     * 
     * @return health check result
     */
    public static HealthCheckResult check() {
        Map<String, Object> details = new HashMap<>();
        
        // Basic system checks
        details.put("uptime_ms", System.currentTimeMillis() - startTime);
        details.put("memory_used_mb", getUsedMemoryMB());
        details.put("memory_max_mb", getMaxMemoryMB());
        details.put("memory_free_mb", getFreeMemoryMB());
        details.put("processors", Runtime.getRuntime().availableProcessors());
        
        // Check OpenTelemetry initialization
        boolean otelInitialized = ObservabilityConfig.getOpenTelemetry() != null;
        details.put("opentelemetry_initialized", otelInitialized);
        
        // Determine overall status
        Status status = Status.HEALTHY;
        if (!otelInitialized) {
            status = Status.UNHEALTHY;
            details.put("error", "OpenTelemetry not initialized");
        }
        
        // Check memory usage
        double memoryUsagePercent = (double) getUsedMemoryMB() / getMaxMemoryMB() * 100;
        details.put("memory_usage_percent", Math.round(memoryUsagePercent * 100.0) / 100.0);
        
        if (memoryUsagePercent > 90) {
            status = Status.UNHEALTHY;
            details.put("warning", "High memory usage: " + memoryUsagePercent + "%");
        }
        
        return new HealthCheckResult(status, details);
    }
    
    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
    
    private static long getMaxMemoryMB() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }
    
    private static long getFreeMemoryMB() {
        return Runtime.getRuntime().freeMemory() / (1024 * 1024);
    }
    
    /**
     * Health check result container.
     */
    public static class HealthCheckResult {
        private final Status status;
        private final Map<String, Object> details;
        private final long timestamp;
        
        public HealthCheckResult(Status status, Map<String, Object> details) {
            this.status = status;
            this.details = new HashMap<>(details);
            this.timestamp = System.currentTimeMillis();
        }
        
        public Status getStatus() {
            return status;
        }
        
        public Map<String, Object> getDetails() {
            return new HashMap<>(details);
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public boolean isHealthy() {
            return status == Status.HEALTHY;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("HealthCheck{status=").append(status);
            sb.append(", timestamp=").append(timestamp);
            sb.append(", details=").append(details);
            sb.append("}");
            return sb.toString();
        }
    }
}
