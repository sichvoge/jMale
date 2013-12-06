/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmale.core.data;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0
 * @since 1.0
 */
public class SimpleDataSet implements DataSet {
    
    private List<DataInstance> instances;
    
    public SimpleDataSet() {
        instances = new ArrayList<DataInstance>();
    }

    public void add(DataInstance instance) {
        Preconditions.checkNotNull(instance);
        
        instances.add(instance);
    }

    public void remove(DataInstance instance) {
        if(!instances.isEmpty()) {
            instances.remove(instance);
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
