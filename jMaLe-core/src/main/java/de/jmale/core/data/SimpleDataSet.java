/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmale.core.data;

import com.google.common.base.Preconditions;
import de.jmale.core.observability.MetricsCollector;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0
 * @since 1.0
 */
public class SimpleDataSet implements DataSet {
    private static final Logger logger = LoggerFactory.getLogger(SimpleDataSet.class);
    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("jmale.dataset");

    private List<DataInstance> instances;
    
    public SimpleDataSet() {
        instances = new ArrayList<DataInstance>();
    }

    public void add(DataInstance instance) {
        Span span = tracer.spanBuilder("dataset.add").startSpan();
        try {
            Preconditions.checkNotNull(instance);

            instances.add(instance);
            span.setAttribute("dataset.size", instances.size());
            logger.trace("Added data instance, dataset size now: {}", instances.size());

            try {
                MetricsCollector.getInstance().incrementOperation("dataset.add");
            } catch (IllegalStateException e) {
                logger.trace("MetricsCollector not initialized, skipping metrics recording");
            }
        } finally {
            span.end();
        }
    }

    public void remove(DataInstance instance) {
        Span span = tracer.spanBuilder("dataset.remove").startSpan();
        try {
            if(!instances.isEmpty()) {
                boolean removed = instances.remove(instance);
                span.setAttribute("dataset.size", instances.size());
                span.setAttribute("instance.removed", removed);
                logger.trace("Removed data instance: {}, dataset size now: {}", removed, instances.size());

                if (removed) {
                    try {
                        MetricsCollector.getInstance().incrementOperation("dataset.remove");
                    } catch (IllegalStateException e) {
                        logger.trace("MetricsCollector not initialized, skipping metrics recording");
                    }
                }
            }
        } finally {
            span.end();
        }
    }

    public List<DataInstance> instances() {
        return instances;
    }

    public DataInstance instanceAt(int index) {
        if(index > instances.size()) {
            throw new IndexOutOfBoundsException();
        }
        
        return instances.get(index);
    }

    public String[] labels() {
        return new String[0];
    }

    public int size() {
        return instances.size();
    }

    public void clear() {
        instances.clear();
    }

}
