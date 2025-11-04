package de.jmale.core.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check utility for monitoring application status.
 * Provides methods to check various application components.
 * 
 * @author OpenTelemetry Instrumentation
 * @version 1.0.0
 * @since 1.0
 */
public class HealthCheck {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);
    
    public enum Status {
        HEALTHY, UNHEALTHY, UNKNOWN
    }
    
    public static class HealthResult {
        private final Status status;
        private final String message;
        private final Map<String, Object> details;
        
        public HealthResult(Status status, String message) {
            this.status = status;
            this.message = message;
            this.details = new HashMap<>();
        }
        
        public HealthResult(Status status, String message, Map<String, Object> details) {
            this.status = status;
            this.message = message;
            this.details = details != null ? details : new HashMap<>();
        }
        
        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public Map<String, Object> getDetails() { return details; }
        
        public boolean isHealthy() { return status == Status.HEALTHY; }
    }
    
    /**
     * Perform a comprehensive health check of the application.
     * 
     * @return overall health result
     */
    public static HealthResult checkHealth() {
        return TracingUtils.trace("health_check", () -> {
            logger.info("Performing application health check");
            
            Map<String, Object> details = new HashMap<>();
            boolean allHealthy = true;
            
            // Check OpenTelemetry initialization
            HealthResult otelCheck = checkOpenTelemetry();
            details.put("opentelemetry", otelCheck);
            if (!otelCheck.isHealthy()) {
                allHealthy = false;
            }
            
            // Check memory usage
            HealthResult memoryCheck = checkMemoryUsage();
            details.put("memory", memoryCheck);
            if (!memoryCheck.isHealthy()) {
                allHealthy = false;
            }
            
            // Check system resources
            HealthResult systemCheck = checkSystemResources();
            details.put("system", systemCheck);
            if (!systemCheck.isHealthy()) {
                allHealthy = false;
            }
            
            Status overallStatus = allHealthy ? Status.HEALTHY : Status.UNHEALTHY;
            String message = allHealthy ? "Application is healthy" : "Application has health issues";
            
            logger.info("Health check completed with status: {}", overallStatus);
            return new HealthResult(overallStatus, message, details);
        });
    }
    
    /**
     * Check OpenTelemetry initialization status.
     * 
     * @return health result for OpenTelemetry
     */
    public static HealthResult checkOpenTelemetry() {
        try {
            OpenTelemetryConfig.getOpenTelemetry();
            return new HealthResult(Status.HEALTHY, "OpenTelemetry is initialized");
        } catch (Exception e) {
            logger.error("OpenTelemetry health check failed", e);
            return new HealthResult(Status.UNHEALTHY, "OpenTelemetry initialization failed: " + e.getMessage());
        }
    }
    
    /**
     * Check memory usage.
     * 
     * @return health result for memory usage
     */
    public static HealthResult checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        Map<String, Object> details = new HashMap<>();
        details.put("max_memory_mb", maxMemory / (1024 * 1024));
        details.put("used_memory_mb", usedMemory / (1024 * 1024));
        details.put("usage_percent", Math.round(memoryUsagePercent * 100.0) / 100.0);
        
        if (memoryUsagePercent > 90) {
            return new HealthResult(Status.UNHEALTHY, "High memory usage: " + memoryUsagePercent + "%", details);
        } else if (memoryUsagePercent > 75) {
            return new HealthResult(Status.HEALTHY, "Moderate memory usage: " + memoryUsagePercent + "%", details);
        } else {
            return new HealthResult(Status.HEALTHY, "Normal memory usage: " + memoryUsagePercent + "%", details);
        }
    }
    
    /**
     * Check system resources.
     * 
     * @return health result for system resources
     */
    public static HealthResult checkSystemResources() {
        Map<String, Object> details = new HashMap<>();
        
        // Check available processors
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        details.put("available_processors", availableProcessors);
        
        // Check Java version
        String javaVersion = System.getProperty("java.version");
        details.put("java_version", javaVersion);
        
        // Check OS information
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        details.put("os_name", osName);
        details.put("os_version", osVersion);
        
        return new HealthResult(Status.HEALTHY, "System resources are available", details);
    }
}
