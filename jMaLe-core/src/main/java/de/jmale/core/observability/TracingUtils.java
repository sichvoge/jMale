package de.jmale.core.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.function.Supplier;

/**
 * Utility class for distributed tracing in jMaLe application.
 * Provides convenient methods for creating and managing spans.
 * 
 * @author OpenTelemetry Instrumentation
 * @version 1.0.0
 * @since 1.0
 */
public class TracingUtils {
    
    private static final Tracer tracer = OpenTelemetryConfig.getTracer();
    
    /**
     * Execute a function within a traced span.
     * 
     * @param spanName the name of the span
     * @param operation the operation to execute
     * @param <T> the return type
     * @return the result of the operation
     */
    public static <T> T trace(String spanName, Supplier<T> operation) {
        return trace(spanName, SpanKind.INTERNAL, operation);
    }
    
    /**
     * Execute a function within a traced span with specified kind.
     * 
     * @param spanName the name of the span
     * @param spanKind the kind of span
     * @param operation the operation to execute
     * @param <T> the return type
     * @return the result of the operation
     */
    public static <T> T trace(String spanName, SpanKind spanKind, Supplier<T> operation) {
        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(spanKind)
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            T result = operation.get();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Execute a runnable within a traced span.
     * 
     * @param spanName the name of the span
     * @param operation the operation to execute
     */
    public static void trace(String spanName, Runnable operation) {
        trace(spanName, SpanKind.INTERNAL, operation);
    }
    
    /**
     * Execute a runnable within a traced span with specified kind.
     * 
     * @param spanName the name of the span
     * @param spanKind the kind of span
     * @param operation the operation to execute
     */
    public static void trace(String spanName, SpanKind spanKind, Runnable operation) {
        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(spanKind)
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            operation.run();
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Create a new span builder.
     *
     * @param spanName the name of the span
     * @return span builder
     */
    public static io.opentelemetry.api.trace.SpanBuilder spanBuilder(String spanName) {
        return tracer.spanBuilder(spanName);
    }
    
    /**
     * Get the current span.
     * 
     * @return current span
     */
    public static Span getCurrentSpan() {
        return Span.current();
    }
    
    /**
     * Add an attribute to the current span.
     * 
     * @param key the attribute key
     * @param value the attribute value
     */
    public static void addAttribute(String key, String value) {
        getCurrentSpan().setAttribute(key, value);
    }
    
    /**
     * Add an attribute to the current span.
     * 
     * @param key the attribute key
     * @param value the attribute value
     */
    public static void addAttribute(String key, long value) {
        getCurrentSpan().setAttribute(key, value);
    }
    
    /**
     * Add an attribute to the current span.
     *
     * @param key the attribute key
     * @param value the attribute value
     */
    public static void addAttribute(String key, double value) {
        getCurrentSpan().setAttribute(key, value);
    }

    /**
     * Add an attribute to the current span.
     *
     * @param key the attribute key
     * @param value the attribute value
     */
    public static void addAttribute(String key, boolean value) {
        getCurrentSpan().setAttribute(key, value);
    }
    
    /**
     * Add an event to the current span.
     * 
     * @param eventName the event name
     */
    public static void addEvent(String eventName) {
        getCurrentSpan().addEvent(eventName);
    }
    
    /**
     * Record an exception in the current span.
     * 
     * @param exception the exception to record
     */
    public static void recordException(Exception exception) {
        getCurrentSpan().recordException(exception);
        getCurrentSpan().setStatus(StatusCode.ERROR, exception.getMessage());
    }
}
