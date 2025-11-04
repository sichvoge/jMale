/**
 * This package contains OpenTelemetry observability instrumentation for the jMaLe application.
 * 
 * <p>The observability package provides comprehensive monitoring capabilities including:
 * <ul>
 *   <li>Distributed tracing for tracking request flows</li>
 *   <li>Metrics collection for performance monitoring</li>
 *   <li>Structured logging with trace correlation</li>
 *   <li>Health checks for application status monitoring</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>Initialize OpenTelemetry at application startup:
 * <pre>{@code
 * OpenTelemetryConfig.initialize();
 * }</pre>
 * 
 * <p>Use tracing utilities to instrument code:
 * <pre>{@code
 * TracingUtils.trace("operation_name", () -> {
 *     // Your code here
 *     return result;
 * });
 * }</pre>
 * 
 * <p>Record metrics for performance monitoring:
 * <pre>{@code
 * MetricsCollector.getInstance().recordSuccess("operation", durationSeconds);
 * }</pre>
 * 
 * <p>Perform health checks:
 * <pre>{@code
 * HealthCheck.HealthResult result = HealthCheck.checkHealth();
 * }</pre>
 * 
 * @author OpenTelemetry Instrumentation
 * @version 1.0.0
 * @since 1.0
 */
package de.jmale.core.observability;
