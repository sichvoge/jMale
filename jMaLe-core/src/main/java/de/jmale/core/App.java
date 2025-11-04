package de.jmale.core;

import de.jmale.core.observability.ObservabilityConfig;
import de.jmale.core.observability.MetricsCollector;
import de.jmale.core.observability.HealthCheck;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Main application class for jMaLe with OpenTelemetry observability.
 *
 */
public class App
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static Tracer tracer;

    public static void main( String[] args )
    {
        long startTime = System.currentTimeMillis();

        // Initialize observability
        OpenTelemetry openTelemetry = ObservabilityConfig.initialize();
        tracer = openTelemetry.getTracer("jmale.app");
        MetricsCollector metricsCollector = MetricsCollector.initialize(openTelemetry);

        // Add application context to logs
        MDC.put("service.name", "jmale");
        MDC.put("service.version", "1.0-SNAPSHOT");

        Span mainSpan = tracer.spanBuilder("app.main").startSpan();
        try {
            logger.info("Starting jMaLe application");

            // Perform health check
            HealthCheck.HealthCheckResult healthResult = HealthCheck.check();
            logger.info("Health check completed: {}", healthResult.getStatus());

            if (!healthResult.isHealthy()) {
                logger.warn("Application health check failed: {}", healthResult.getDetails());
            }

            // Main application logic
            System.out.println( "Hello World!" );
            logger.info("Application message displayed successfully");

            // Record metrics
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordOperation("app.startup", duration);
            metricsCollector.incrementOperation("app.main.execution");

            logger.info("jMaLe application completed successfully in {} ms", duration);

        } catch (Exception e) {
            logger.error("Application failed with error", e);
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordError("app.startup", duration, e.getClass().getSimpleName());
            mainSpan.recordException(e);
            throw e;
        } finally {
            mainSpan.end();
            MDC.clear();

            // Shutdown observability
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down observability");
                ObservabilityConfig.shutdown();
            }));
        }
    }
}
