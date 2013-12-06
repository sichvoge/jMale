package de.jmale.core.data;

import java.util.List;

/**
 *
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0 2012-03-30
 * @since 1.0
 */
public interface DataSet {
    void add(DataInstance instance);
    
    void remove(DataInstance instance);
    
    List<DataInstance> instances();
    
    DataInstance instanceAt(int index);
    
    String[] labels();
    
    int size();
    
    void clear();
}
