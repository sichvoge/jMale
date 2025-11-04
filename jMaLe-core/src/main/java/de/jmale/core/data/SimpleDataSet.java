/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmale.core.data;

import com.google.common.base.Preconditions;
import de.jmale.core.observability.MetricsCollector;
import de.jmale.core.observability.TracingUtils;
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

    private List<DataInstance> instances;

    public SimpleDataSet() {
        instances = new ArrayList<DataInstance>();
        logger.debug("Created new SimpleDataSet");
    }

    public void add(DataInstance instance) {
        TracingUtils.trace("dataset_add_instance", () -> {
            Preconditions.checkNotNull(instance);

            instances.add(instance);
            TracingUtils.addAttribute("dataset_size", instances.size());
            MetricsCollector.getInstance().recordDataProcessing("add_instance", "success");
            logger.debug("Added data instance to dataset, new size: {}", instances.size());
        });
    }

    public void remove(DataInstance instance) {
        TracingUtils.trace("dataset_remove_instance", () -> {
            if(!instances.isEmpty()) {
                boolean removed = instances.remove(instance);
                if (removed) {
                    TracingUtils.addAttribute("dataset_size", instances.size());
                    MetricsCollector.getInstance().recordDataProcessing("remove_instance", "success");
                    logger.debug("Removed data instance from dataset, new size: {}", instances.size());
                } else {
                    logger.debug("Data instance not found for removal");
                }
            }
        });
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
        TracingUtils.trace("dataset_clear", () -> {
            int previousSize = instances.size();
            instances.clear();
            TracingUtils.addAttribute("cleared_instances", previousSize);
            MetricsCollector.getInstance().recordDataProcessing("clear_dataset", "success");
            logger.debug("Cleared dataset, removed {} instances", previousSize);
        });
    }

}
