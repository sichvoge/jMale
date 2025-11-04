package de.jmale.core;

import de.jmale.core.observability.HealthCheck;
import de.jmale.core.observability.MetricsCollector;
import de.jmale.core.observability.OpenTelemetryConfig;
import de.jmale.core.observability.TracingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for jMaLe.
 * Initializes OpenTelemetry and demonstrates basic functionality.
 */
public class App
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        // Initialize OpenTelemetry
        OpenTelemetryConfig.initialize();
        logger.info("jMaLe application starting up");

        TracingUtils.trace("main_application", () -> {
            long startTime = System.currentTimeMillis();

            try {
                logger.info("Hello World from jMaLe!");

                // Perform health check
                HealthCheck.HealthResult healthResult = HealthCheck.checkHealth();
                logger.info("Application health status: {}", healthResult.getStatus());

                // Record metrics
                double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                MetricsCollector.getInstance().recordSuccess("application_startup", durationSeconds);

                logger.info("jMaLe application completed successfully");

            } catch (Exception e) {
                logger.error("Application error occurred", e);
                double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                MetricsCollector.getInstance().recordError("application_startup", durationSeconds);
                TracingUtils.recordException(e);
                throw e;
            }
        });
    }
}
